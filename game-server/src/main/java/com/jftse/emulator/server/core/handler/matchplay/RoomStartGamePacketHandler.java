package com.jftse.emulator.server.core.handler.matchplay;

import com.jftse.emulator.server.core.client.PetView;
import com.jftse.emulator.server.core.constants.MiscConstants;
import com.jftse.emulator.server.core.constants.RoomStatus;
import com.jftse.emulator.server.core.life.room.GameSession;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.matchplay.GameSessionManager;
import com.jftse.emulator.server.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.core.matchplay.game.MatchplayBasicGame;
import com.jftse.emulator.server.core.matchplay.game.MatchplayBattleGame;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.core.matchplay.extension.MatchplayLifecycleExtension;
import com.jftse.emulator.server.core.packets.chat.S2CChatRoomAnswerPacket;
import com.jftse.emulator.server.core.packets.lobby.room.S2CPetRequestRoomAnswerPacket;
import com.jftse.emulator.server.core.packets.lobby.room.S2CRoomPlayerListInformationPacket;
import com.jftse.emulator.server.core.utils.MatchRelayRegistration;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.gameserver.GameServer;
import com.jftse.server.core.constants.GameMode;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.AuthenticationService;
import com.jftse.server.core.shared.ServerConfService;
import com.jftse.server.core.shared.packets.matchplay.*;
import com.jftse.server.core.thread.ThreadManager;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Log4j2
@PacketId(CMSGStartGame.PACKET_ID)
public class RoomStartGamePacketHandler implements PacketHandler<FTConnection, CMSGStartGame> {
    private final AuthenticationService authenticationService;
    private final ServerConfService serverConfService;

    public RoomStartGamePacketHandler() {
        this.authenticationService = ServiceManager.getInstance().getAuthenticationService();
        this.serverConfService = GameManager.getInstance().getServerConfService();
    }

    @Override
    public void handle(FTConnection connection, CMSGStartGame packet) {
        Packet roomStartGameAck = new Packet(PacketOperations.S2CRoomStartGameAck);
        roomStartGameAck.write((char) 0);

        FTClient ftClient = connection.getClient();

        if (ftClient == null || !ftClient.hasPlayer()) {
            connection.sendTCP(roomStartGameAck);
            return;
        }

        Room room = ftClient.getActiveRoom();
        if (room == null) {
            connection.sendTCP(roomStartGameAck);
            return;
        }

        for (MatchplayLifecycleExtension ext : ServiceManager.getInstance().getMatchplayLifecycleExtensions()) {
            String rejectionReason = ext.validateMatchStart(room);
            if (rejectionReason != null) {
                S2CChatRoomAnswerPacket rejectionPacket = new S2CChatRoomAnswerPacket((byte) 2, "Room", rejectionReason);
                GameManager.getInstance().getClientsInRoom(room.getRoomId()).forEach(c -> c.getConnection().sendTCP(rejectionPacket));
                connection.sendTCP(roomStartGameAck);
                return;
            }
        }

        synchronized (room) {
            if (room.getStatus() != RoomStatus.NotRunning || ftClient.getRoomPlayer() == null || !ftClient.getRoomPlayer().isMaster()) {
                connection.sendTCP(roomStartGameAck);
                return;
            }

            room.setStatus(RoomStatus.StartingGame);
        }

        GameServer relayServer = authenticationService.getGameServerByPort(this.serverConfService.get("RelayPort", Integer.class));

        List<FTClient> clientsInRoom = GameManager.getInstance().getClientsInRoom(room.getRoomId()).stream()
                .filter(c -> c.hasPlayer() && c.getRoomPlayer() != null && c.getConnection() != null)
                .toList();

        GameSession gameSession = new GameSession();
        Integer gameSessionId = GameSessionManager.getInstance().addGameSession(gameSession);

        gameSession.setPlayers(room.getPlayers());
        MatchplayGame game;
        switch (room.getMode()) {
            case GameMode.BASIC -> game = new MatchplayBasicGame(room.getPlayers());
            case GameMode.BATTLE -> game = new MatchplayBattleGame(room.getPlayers());
            case GameMode.GUARDIAN -> game = new MatchplayGuardianGame();
            default -> throw new IllegalStateException("room mode not supported: " + room.getMode());
        }
        gameSession.setMatchplayGame(game);

        clientsInRoom.forEach(c -> {
            c.setActiveGameSession(gameSessionId);
            gameSession.getClients().add(c);
            c.getRoomPlayer().getConnectedToRelay().set(false);
        });

        SMSGClearMatchHost clearHostPacket = SMSGClearMatchHost.builder().enabled(false).build();
        clientsInRoom.forEach(c -> {
            c.getConnection().sendTCP(clearHostPacket);

            SMSGConnectMatchRelay relaySettings = MatchRelayRegistration.create(relayServer.getHost(), relayServer.getPort(), gameSessionId, c, clientsInRoom);
            c.getConnection().sendTCP(relaySettings);
        });

        int initialRoomPlayerSize = room.getRoomPlayerList().size();

        ThreadManager.getInstance().newTask(() -> {
            Room threadRoom = room;
            if (ftClient.getActiveRoom() != room || ftClient.getActiveGameSession() != gameSession)
                return;
            if (threadRoom != null) {
                long relayDeadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
                while (threadRoom.getStatus() != RoomStatus.RelayConnectionSuccess) {
                    if (ftClient.getActiveRoom() != room || ftClient.getActiveGameSession() != gameSession)
                        return;
                    boolean allReady = threadRoom.getRoomPlayerList().stream()
                            .filter(rp -> !rp.isMaster() && rp.getPosition() >= 0 && rp.getPosition() < 4)
                            .allMatch(RoomPlayer::isReady);

                    boolean roomPlayerSizeChanged = initialRoomPlayerSize != threadRoom.getRoomPlayerList().size();

                    synchronized (threadRoom) {
                        if (!allReady || roomPlayerSizeChanged || System.nanoTime() - relayDeadline >= 0 || threadRoom.getStatus() == RoomStatus.StartCancelled || threadRoom.getStatus() == RoomStatus.RelayConnectionFailed) {
                            threadRoom.setStatus(RoomStatus.NotRunning);
                            SMSGCancelStartGame cancelStartGamePacket = SMSGCancelStartGame.builder().result((char) 0).build();

                            threadRoom.getRoomPlayerList().forEach(rp -> {
                                rp.setReady(false);
                                rp.getConnectedToRelay().set(false);
                            });
                            clientsInRoom.forEach(c -> {
                                if (c.getActiveGameSession() == gameSession)
                                    c.setActiveGameSession(null);
                            });

                            GameSessionManager.getInstance().removeGameSession(gameSessionId, gameSession);

                            RoomPlayer threadRoomPlayer = threadRoom.getRoomPlayerList().stream()
                                    .filter(x -> x.getPlayerId() == ftClient.getPlayer().getId())
                                    .findFirst()
                                    .orElse(null);

                            List<RoomPlayer> filteredRoomPlayerList = threadRoomPlayer == null || threadRoomPlayer.getPosition() == MiscConstants.InvisibleGmSlot
                                    ? threadRoom.getRoomPlayerList().stream().toList()
                                    : threadRoom.getRoomPlayerList().stream()
                                            .filter(x -> x.getPosition() != MiscConstants.InvisibleGmSlot)
                                            .toList();
                            S2CRoomPlayerListInformationPacket roomPlayerInformationPacket = new S2CRoomPlayerListInformationPacket(filteredRoomPlayerList);
                            GameManager.getInstance().getClientsInRoom(threadRoom.getRoomId()).forEach(c -> {
                                if (c.getConnection() != null) {
                                    c.getConnection().sendTCP(roomPlayerInformationPacket);

                                    for (RoomPlayer rp : filteredRoomPlayerList) {
                                        PetView pet = rp.getPet();
                                        if (pet != null) {
                                            byte slot = rp.getPosition() == 0 ? (byte) 0 : (byte) 1;
                                            S2CPetRequestRoomAnswerPacket petRequestRoomAnswerPacket = new S2CPetRequestRoomAnswerPacket(S2CPetRequestRoomAnswerPacket.SUCCESS, true, slot, pet);
                                            c.getConnection().sendTCP(petRequestRoomAnswerPacket);
                                        }
                                    }
                                }
                            });

                            GameManager.getInstance().updateRoomForAllClientsInMultiplayer(ftClient.getConnection(), threadRoom);
                            GameManager.getInstance().getClientsInRoom(threadRoom.getRoomId()).forEach(c -> {
                                if (c.getConnection() != null) {
                                    c.getConnection().sendTCP(cancelStartGamePacket);
                                    c.getConnection().sendTCP(roomStartGameAck);
                                    c.getConnection().sendTCP(clearHostPacket);
                                }
                            });
                            return;
                        }
                    }
                    try {
                        TimeUnit.MILLISECONDS.sleep(1000);
                    } catch (InterruptedException e) {
                        log.error("Interrupted while waiting for relay connection success", e);
                        synchronized (threadRoom) {
                            threadRoom.setStatus(RoomStatus.RelayConnectionFailed);
                        }
                        Thread.currentThread().interrupt();
                    }
                }
            }

            synchronized (room) {
                if (room.getStatus() != RoomStatus.RelayConnectionSuccess || ftClient.getActiveGameSession() != gameSession)
                    return;
                room.setStatus(RoomStatus.InitializingGame);
            }

            RoomPlayer playerInSlot0 = room.getRoomPlayerList().stream()
                    .filter(x -> x.getPosition() == 0)
                    .findFirst().orElse(null);
            FTClient clientToHostGame = GameManager.getInstance().getClientsInRoom(room.getRoomId()).stream()
                    .filter(x -> playerInSlot0 != null && x.hasPlayer() && x.getPlayer().getId() == playerInSlot0.getPlayerId())
                    .findFirst()
                    .orElse(connection.getClient());
            SMSGSetMatchHost setHostPacket = SMSGSetMatchHost.builder().isHost(true).build();
            clientToHostGame.getConnection().sendTCP(setHostPacket);

            SMSGSetHostReference hostReferencePacket = SMSGSetHostReference.builder().build();
            clientToHostGame.getConnection().sendTCP(hostReferencePacket);

            game.getHandleable().onPrepare(ftClient);

            SMSGStartGame startGamePacket = SMSGStartGame.builder().result((char) 0).build();

            GameManager.getInstance().sendPacketToAllClientsInSameGameSession(startGamePacket, ftClient.getConnection());
        });
    }
}

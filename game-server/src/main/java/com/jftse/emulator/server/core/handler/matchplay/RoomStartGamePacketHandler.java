package com.jftse.emulator.server.core.handler.matchplay;

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
import com.jftse.emulator.server.core.packets.lobby.room.S2CRoomPlayerListInformationPacket;
import com.jftse.emulator.server.core.packets.matchplay.S2CGameNetworkSettingsPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.entities.database.model.gameserver.GameServer;
import com.jftse.server.core.constants.GameMode;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.AuthenticationService;
import com.jftse.server.core.thread.ThreadManager;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Log4j2
@PacketOperationIdentifier(PacketOperations.C2SRoomTriggerStartGame)
public class RoomStartGamePacketHandler extends AbstractPacketHandler {
    private Packet packet;

    private final AuthenticationService authenticationService;

    public RoomStartGamePacketHandler() {
        this.authenticationService = ServiceManager.getInstance().getAuthenticationService();
    }

    @Override
    public boolean process(Packet packet) {
        this.packet = packet;
        return true;
    }

    @Override
    public void handle() {
        Packet roomStartGameAck = new Packet(PacketOperations.S2CRoomStartGameAck);
        roomStartGameAck.write((char) 0);

        FTClient ftClient = (FTClient) connection.getClient();

        if (ftClient == null) {
            connection.sendTCP(roomStartGameAck);
            return;
        }

        Room room = ftClient.getActiveRoom();
        if (room == null) {
            connection.sendTCP(roomStartGameAck);
            return;
        }

        synchronized (room) {
            if (room.getStatus() != RoomStatus.NotRunning) {
                connection.sendTCP(roomStartGameAck);
                return;
            }

            room.setStatus(RoomStatus.StartingGame);
        }

        GameServer relayServer = authenticationService.getGameServerByPort(5895 + 1);

        List<FTClient> clientsInRoom = new ArrayList<>(GameManager.getInstance().getClientsInRoom(room.getRoomId()));

        GameSession gameSession = new GameSession();
        Integer gameSessionId = GameSessionManager.getInstance().addGameSession(gameSession);

        gameSession.setPlayers(room.getPlayers());
        MatchplayGame game;
        switch (room.getMode()) {
            case GameMode.BASIC -> game = new MatchplayBasicGame(room.getPlayers());
            case GameMode.BATTLE -> game = new MatchplayBattleGame();
            case GameMode.GUARDIAN -> game = new MatchplayGuardianGame();
            default -> throw new IllegalStateException("room mode not supported: " + room.getMode());
        }
        gameSession.setMatchplayGame(game);

        clientsInRoom.forEach(c -> {
            c.setActiveGameSession(gameSessionId);
            gameSession.getClients().add(c);
        });

        Packet unsetHostPacket = new Packet(PacketOperations.S2CUnsetHost);
        unsetHostPacket.write((byte) 0);

        List<FTClient> clientInRoomLeftShiftList = new ArrayList<>(clientsInRoom);
        clientsInRoom.forEach(c -> {
            c.getConnection().sendTCP(unsetHostPacket);

            S2CGameNetworkSettingsPacket gameNetworkSettings = new S2CGameNetworkSettingsPacket(relayServer.getHost(), relayServer.getPort(), gameSessionId, room, clientInRoomLeftShiftList);
            c.getConnection().sendTCP(gameNetworkSettings);

            // shift list to the left, so every client has his player id in the first place when doing session register
            clientInRoomLeftShiftList.add(0, clientInRoomLeftShiftList.remove(clientInRoomLeftShiftList.size() - 1));
        });

        int initialRoomPlayerSize = room.getRoomPlayerList().size();

        ThreadManager.getInstance().schedule(() -> {
            Room threadRoom = ftClient.getActiveRoom();
            if (threadRoom != null) {
                while (threadRoom.getStatus() != RoomStatus.RelayConnectionSuccess) {
                    boolean allReady = threadRoom.getRoomPlayerList().stream()
                            .filter(rp -> !rp.isMaster())
                            .collect(Collectors.toList())
                            .stream()
                            .filter(rp -> rp.getPosition() < 4)
                            .allMatch(RoomPlayer::isReady);

                    boolean roomPlayerSizeChanged = initialRoomPlayerSize != threadRoom.getRoomPlayerList().size();

                    synchronized (threadRoom) {
                        if (!allReady || roomPlayerSizeChanged || threadRoom.getStatus() == RoomStatus.StartCancelled || threadRoom.getStatus() == RoomStatus.RelayConnectionFailed) {
                            threadRoom.setStatus(RoomStatus.NotRunning);
                            Packet startGameCancelledPacket = new Packet(PacketOperations.S2CRoomStartGameCancelled);
                            startGameCancelledPacket.write((char) 0);

                            threadRoom.getRoomPlayerList().forEach(rp -> {
                                rp.setReady(false);
                                rp.getConnectedToRelay().set(false);
                            });
                            clientsInRoom.forEach(c -> c.setActiveGameSession(null));

                            GameSessionManager.getInstance().removeGameSession(gameSessionId, gameSession);

                            S2CRoomPlayerListInformationPacket roomPlayerInformationPacket = new S2CRoomPlayerListInformationPacket(new ArrayList<>(threadRoom.getRoomPlayerList()));
                            GameManager.getInstance().getClientsInRoom(threadRoom.getRoomId()).forEach(c -> {
                                if (c.getConnection() != null) {
                                    c.getConnection().sendTCP(roomPlayerInformationPacket);
                                }
                            });
                            GameManager.getInstance().updateRoomForAllClientsInMultiplayer(ftClient.getConnection(), threadRoom);
                            GameManager.getInstance().getClientsInRoom(threadRoom.getRoomId()).forEach(c -> {
                                if (c.getConnection() != null) {
                                    c.getConnection().sendTCP(startGameCancelledPacket);
                                    c.getConnection().sendTCP(roomStartGameAck);
                                    c.getConnection().sendTCP(unsetHostPacket);
                                }
                            });
                            return;
                        }
                    }
                    try {
                        TimeUnit.MILLISECONDS.sleep(1000);
                    } catch (InterruptedException e) {
                        log.error("Error while waiting for relay connection success", e);
                    }
                }
            }

            RoomPlayer playerInSlot0 = room.getRoomPlayerList().stream()
                    .filter(x -> x.getPosition() == 0)
                    .findFirst().orElse(null);
            FTClient clientToHostGame = GameManager.getInstance().getClientsInRoom(room.getRoomId()).stream()
                    .filter(x -> playerInSlot0 != null && x.getPlayer() != null && x.getPlayer().getId().equals(playerInSlot0.getPlayer().getId()))
                    .findFirst()
                    .orElse((FTClient) connection.getClient());
            Packet setHostPacket = new Packet(PacketOperations.S2CSetHost);
            setHostPacket.write((byte) 1);
            clientToHostGame.getConnection().sendTCP(setHostPacket);

            Packet setHostUnknownPacket = new Packet(PacketOperations.S2CSetHostUnknown);
            clientToHostGame.getConnection().sendTCP(setHostUnknownPacket);

            game.getHandleable().onPrepare(ftClient);

            Packet startGamePacket = new Packet(PacketOperations.S2CRoomStartGame);
            startGamePacket.write((char) 0);

            synchronized (room) {
                room.setStatus(RoomStatus.InitializingGame);
            }
            GameManager.getInstance().sendPacketToAllClientsInSameGameSession(startGamePacket, ftClient.getConnection());
        }, 0, TimeUnit.SECONDS);
    }
}

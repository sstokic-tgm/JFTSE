package com.jftse.emulator.server.core.handler.game.matchplay;

import com.jftse.emulator.server.core.constants.GameMode;
import com.jftse.emulator.server.core.constants.RoomStatus;
import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.handler.game.matchplay.prepare.PrepareBattleMode;
import com.jftse.emulator.server.core.handler.game.matchplay.prepare.PrepareGuardianMode;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.manager.ThreadManager;
import com.jftse.emulator.server.core.matchplay.GameSessionManager;
import com.jftse.emulator.server.core.matchplay.game.MatchplayBasicGame;
import com.jftse.emulator.server.core.matchplay.game.MatchplayBattleGame;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.core.matchplay.room.GameSession;
import com.jftse.emulator.server.core.matchplay.room.Room;
import com.jftse.emulator.server.core.matchplay.room.RoomPlayer;
import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.core.packet.packets.chat.S2CChatRoomAnswerPacket;
import com.jftse.emulator.server.core.packet.packets.lobby.room.S2CRoomPlayerInformationPacket;
import com.jftse.emulator.server.core.packet.packets.matchplay.S2CGameNetworkSettingsPacket;
import com.jftse.emulator.server.core.service.AuthenticationService;
import com.jftse.emulator.server.database.model.gameserver.GameServer;
import com.jftse.emulator.server.networking.packet.Packet;
import com.jftse.emulator.server.shared.module.Client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class RoomStartGamePacketHandler extends AbstractHandler {
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
        Packet roomStartGameAck = new Packet(PacketOperations.S2CRoomStartGameAck.getValueAsChar());
        roomStartGameAck.write((char) 0);

        if (connection.getClient() == null) {
            connection.sendTCP(roomStartGameAck);
            return;
        }

        Room room = connection.getClient().getActiveRoom();
        if (room == null) {
            connection.sendTCP(roomStartGameAck);
            return;
        }

        synchronized (room) {
            if (room.getStatus() == RoomStatus.StartingGame) {
                connection.sendTCP(roomStartGameAck);
                room.setStatus(RoomStatus.StartCancelled);
                return;
            }

            if (room.getStatus() != RoomStatus.NotRunning) {
                connection.sendTCP(roomStartGameAck);
                return;
            }

            room.setStatus(RoomStatus.StartingGame);
        }

        GameServer relayServer = authenticationService.getGameServerByPort(connection.getServer().getTcpPort() + 1);

        List<Client> clientsInRoom = new ArrayList<>(GameManager.getInstance().getClientsInRoom(room.getRoomId()));

        GameSession gameSession = new GameSession();
        gameSession.setSessionId(room.getRoomId());
        gameSession.setPlayers(room.getPlayers());
        switch (room.getMode()) {
            case GameMode.BASIC -> gameSession.setActiveMatchplayGame(new MatchplayBasicGame(room.getPlayers()));
            case GameMode.BATTLE -> gameSession.setActiveMatchplayGame(new MatchplayBattleGame());
            case GameMode.GUARDIAN -> gameSession.setActiveMatchplayGame(new MatchplayGuardianGame());
        }

        clientsInRoom.forEach(c -> c.setActiveGameSession(gameSession));

        gameSession.setClients(new ArrayList<>(clientsInRoom));
        GameSessionManager.getInstance().addGameSession(gameSession);

        List<Client> clientInRoomLeftShiftList = new ArrayList<>(clientsInRoom);
        clientsInRoom.forEach(c -> {
            Packet unsetHostPacket = new Packet(PacketOperations.S2CUnsetHost.getValueAsChar());
            unsetHostPacket.write((byte) 0);
            c.getConnection().sendTCP(unsetHostPacket);

            S2CGameNetworkSettingsPacket gameNetworkSettings = new S2CGameNetworkSettingsPacket(relayServer.getHost(), relayServer.getPort(), room, clientInRoomLeftShiftList);
            c.getConnection().sendTCP(gameNetworkSettings);

            // shift list to the left, so every client has his player id in the first place when doing session register
            clientInRoomLeftShiftList.add(0, clientInRoomLeftShiftList.remove(clientInRoomLeftShiftList.size() - 1));
        });

        int initialRoomPlayerSize = room.getRoomPlayerList().size();

        ThreadManager.getInstance().schedule(() -> {
            int secondsToCount = 5;
            for (int i = 0; i < secondsToCount; i++) {
                Room threadRoom = connection.getClient().getActiveRoom();
                if (threadRoom != null) {
                    boolean allReady = threadRoom.getRoomPlayerList().stream()
                            .filter(rp -> !rp.isMaster())
                            .collect(Collectors.toList())
                            .stream()
                            .filter(rp -> rp.getPosition() < 4)
                            .allMatch(RoomPlayer::isReady);

                    boolean roomPlayerSizeChanged = initialRoomPlayerSize != threadRoom.getRoomPlayerList().size();

                    synchronized (threadRoom) {
                        if (!allReady || roomPlayerSizeChanged || threadRoom.getStatus() == RoomStatus.StartCancelled) {
                            threadRoom.setStatus(RoomStatus.NotRunning);
                            Packet startGameCancelledPacket = new Packet(PacketOperations.S2CRoomStartGameCancelled.getValueAsChar());
                            startGameCancelledPacket.write((char) 0);

                            threadRoom.getRoomPlayerList().stream()
                                    .filter(rp -> !rp.isMaster())
                                    .forEach(rp -> rp.setReady(false));
                            clientsInRoom.forEach(c -> c.setActiveGameSession(null));

                            GameSessionManager.getInstance().removeGameSession(gameSession);

                            S2CRoomPlayerInformationPacket roomPlayerInformationPacket = new S2CRoomPlayerInformationPacket(new ArrayList<>(threadRoom.getRoomPlayerList()));
                            GameManager.getInstance().getClientsInRoom(threadRoom.getRoomId()).forEach(c -> {
                                if (c.getConnection() != null && c.getConnection().isConnected()) {
                                    c.getConnection().sendTCP(roomPlayerInformationPacket);
                                }
                            });
                            GameManager.getInstance().updateRoomForAllClientsInMultiplayer(connection, threadRoom);
                            GameManager.getInstance().getClientsInRoom(threadRoom.getRoomId()).forEach(c -> {
                                if (c.getConnection() != null && c.getConnection().isConnected()) {
                                    c.getConnection().sendTCP(startGameCancelledPacket);
                                }
                            });
                            return;
                        }
                    }
                }

                String message = String.format("Game starting in %s...", secondsToCount - i);
                S2CChatRoomAnswerPacket chatRoomAnswerPacket = new S2CChatRoomAnswerPacket((byte) 2, "Room", message);
                GameManager.getInstance().sendPacketToAllClientsInSameGameSession(chatRoomAnswerPacket, connection);
                try {
                    TimeUnit.MILLISECONDS.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            RoomPlayer playerInSlot0 = room.getRoomPlayerList().stream()
                    .filter(x -> x.getPosition() == 0)
                    .findFirst().orElse(null);
            Client clientToHostGame = GameManager.getInstance().getClientsInRoom(room.getRoomId()).stream()
                    .filter(x -> playerInSlot0 != null && x.getActivePlayer() != null && x.getActivePlayer().getId().equals(playerInSlot0.getPlayer().getId()))
                    .findFirst()
                    .orElse(connection.getClient());
            Packet setHostPacket = new Packet(PacketOperations.S2CSetHost.getValueAsChar());
            setHostPacket.write((byte) 1);
            clientToHostGame.getConnection().sendTCP(setHostPacket);

            Packet setHostUnknownPacket = new Packet(PacketOperations.S2CSetHostUnknown.getValueAsChar());
            clientToHostGame.getConnection().sendTCP(setHostUnknownPacket);

            switch (room.getMode()) {
                case GameMode.BATTLE -> {
                    PrepareBattleMode prepareBattleMode = new PrepareBattleMode();
                    try {
                        prepareBattleMode.setConnection(connection);
                        if (prepareBattleMode.process(packet))
                            prepareBattleMode.handle();
                    } catch (Exception e) {
                        connection.notifyException(e);
                    }
                }
                case GameMode.GUARDIAN -> {
                    PrepareGuardianMode prepareGuardianMode = new PrepareGuardianMode();
                    try {
                        prepareGuardianMode.setConnection(connection);
                        if (prepareGuardianMode.process(packet))
                            prepareGuardianMode.handle();
                    } catch (Exception e) {
                        connection.notifyException(e);
                    }
                }
            }

            Packet startGamePacket = new Packet(PacketOperations.S2CRoomStartGame.getValueAsChar());
            startGamePacket.write((char) 0);

            synchronized (room) {
                room.setStatus(RoomStatus.InitializingGame);
            }
            GameManager.getInstance().sendPacketToAllClientsInSameGameSession(startGamePacket, connection);
        }, 0, TimeUnit.SECONDS);

        connection.sendTCP(roomStartGameAck);
    }
}

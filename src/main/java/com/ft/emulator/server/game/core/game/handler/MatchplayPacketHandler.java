package com.ft.emulator.server.game.core.game.handler;

import com.ft.emulator.server.game.core.constants.GameFieldSide;
import com.ft.emulator.server.game.core.constants.PacketEventType;
import com.ft.emulator.server.game.core.matchplay.GameSessionManager;
import com.ft.emulator.server.game.core.matchplay.basic.MatchplayBasicSingleGame;
import com.ft.emulator.server.game.core.matchplay.event.PacketEvent;
import com.ft.emulator.server.game.core.matchplay.event.PacketEventHandler;
import com.ft.emulator.server.game.core.matchplay.room.GameSession;
import com.ft.emulator.server.game.core.matchplay.room.RoomPlayer;
import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.game.core.packet.packets.S2CWelcomePacket;
import com.ft.emulator.server.game.core.packet.packets.matchplay.C2SMatchplayPlayerIdsInSessionPacket;
import com.ft.emulator.server.game.core.packet.packets.matchplay.S2CMatchplayTeamWinsPoint;
import com.ft.emulator.server.game.core.packet.packets.matchplay.S2CMatchplayTeamWinsSet;
import com.ft.emulator.server.game.core.packet.packets.matchplay.S2CMatchplayTriggerServe;
import com.ft.emulator.server.game.core.packet.packets.matchplay.relay.C2CBallAnimationPacket;
import com.ft.emulator.server.game.core.packet.packets.matchplay.relay.C2CPlayerAnimationPacket;
import com.ft.emulator.server.networking.Connection;
import com.ft.emulator.server.networking.packet.Packet;
import com.ft.emulator.server.shared.module.Client;
import com.ft.emulator.server.shared.module.RelayHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class MatchplayPacketHandler {
    private final GameSessionManager gameSessionManager;
    private final RelayHandler relayHandler;
    private final PacketEventHandler packetEventHandler;

    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    public void init() {
        scheduledExecutorService.scheduleAtFixedRate(this::handleQueuedPackets, 0, 1, TimeUnit.MILLISECONDS);
    }

    public RelayHandler getRelayHandler() {
        return relayHandler;
    }

    public void sendWelcomePacket(Connection connection) {
        S2CWelcomePacket welcomePacket = new S2CWelcomePacket(0, 0, 0, 0);
        connection.sendTCP(welcomePacket);
    }

    public void handleRelayPacketToClientsInGameSessionRequest(Connection connection, Packet packet) {
        Packet relayPacket = new Packet(packet.getData());

        GameSession gameSession = connection.getClient().getActiveGameSession();

        switch (relayPacket.getPacketId()) {
            case PacketID.C2CBallAnimationPacket:
                C2CBallAnimationPacket ballAnimationPacket = new C2CBallAnimationPacket(relayPacket);
                gameSession.setTimeLastBallWasHit(Instant.now().toEpochMilli());
                gameSession.setLastBallHitByTeam(ballAnimationPacket.getPlayerPosition());

                packetEventHandler.push(createPacketEvent(connection.getClient(), ballAnimationPacket, PacketEventType.DEFAULT, TimeUnit.SECONDS.toMillis(3)), PacketEventHandler.ServerClient.CLIENT);
                break;
            case PacketID.C2CPlayerAnimationPacket:
                C2CPlayerAnimationPacket playerAnimationPacket = new C2CPlayerAnimationPacket(relayPacket);

                packetEventHandler.push(createPacketEvent(connection.getClient(), playerAnimationPacket, PacketEventType.DEFAULT, 0), PacketEventHandler.ServerClient.CLIENT);
                break;
        }

        sendPacketToAllClientInSameGameSession(connection, relayPacket);
    }

    public void handleRegisterPlayerForSession(Connection connection, Packet packet) {
        C2SMatchplayPlayerIdsInSessionPacket matchplayPlayerIdsInSessionPacket = new C2SMatchplayPlayerIdsInSessionPacket(packet);

        int playerId = matchplayPlayerIdsInSessionPacket.getPlayerIds().stream().findFirst().orElse(-1);
        int sessionId = matchplayPlayerIdsInSessionPacket.getSessionId();

        GameSession gameSession = this.gameSessionManager.getGameSessionBySessionId(sessionId);
        if (gameSession != null) {
            Client playerClient = gameSession.getClientByPlayerId(playerId);
            playerClient.setActiveGameSession(gameSession);
            playerClient.setRelayConnection(connection);
            connection.setClient(playerClient);
            this.relayHandler.addClient(playerClient);

            Packet answer = new Packet(PacketID.S2CMatchplayAckPlayerInformation);
            answer.write((byte) 0);
            connection.sendTCP(answer);
        }
        else {
            // disconnect all clients maybe? put them back to the room mybe?
        }
    }

    public void handleDisconnect(Connection connection) {
        Client client = connection.getClient();
        if (client == null) return; // server checker will throw null here, since we don't register a client for the connection,
                                    // because originally we want that to do inside handleRegisterPlayerForSession, need solution
        GameSession gameSession = client.getActiveGameSession();
        if (gameSession == null) return;

        client.setActiveGameSession(null);
        gameSession.getClients().removeIf(x -> x.getRelayConnection().getId() == connection.getId());
        if (gameSession.getClients().size() == 0) {
            this.relayHandler.getSessionList().remove(gameSession);
        }
    }

    public void handleUnknown(Connection connection, Packet packet) {
        Packet unknownAnswer = new Packet((char) (packet.getPacketId() + 1));
        if (unknownAnswer.getPacketId() == (char) 0x200E) {
            unknownAnswer.write((char) 1);
        }
        else {
            unknownAnswer.write((short) 0);
        }
        connection.sendTCP(unknownAnswer);
    }

    private void sendPacketToAllClientInSameGameSession(Connection connection, Packet packet) {
        List<Client> clientList = relayHandler.getClientsInGameSession(connection.getClient().getActiveGameSession().getSessionId());
        for (Client client : clientList) {
            client.getRelayConnection().sendTCP(packet);
        }
    }

    private void handleGameSessionState(PacketEvent packetEvent, long currentTime) {
        Connection connection = packetEvent.getSender();
        GameSession gameSession = packetEvent.getClient().getActiveGameSession();
        if (gameSession == null) return;
        if (gameSession.getTimeLastBallWasHit() == -1) return;
        if (gameSession.getLastBallHitByTeam() == -1) return;

        if (currentTime - gameSession.getTimeLastBallWasHit() > packetEvent.getEventFireTime()) {
            // We need to branch here later for different modes. Best would be without casting haha
            MatchplayBasicSingleGame game = (MatchplayBasicSingleGame) gameSession.getActiveMatchplayGame();
            byte setsTeamRead = game.getSetsPlayer1();
            byte setsTeamBlue = game.getSetsPlayer2();
            if (gameSession.getLastBallHitByTeam() == GameFieldSide.RedTeam) {
                game.setPoints((byte) (game.getPointsPlayer1() + 1), game.getPointsPlayer2());
            } else if (gameSession.getLastBallHitByTeam() == GameFieldSide.BlueTeam) {
                game.setPoints(game.getPointsPlayer1(), (byte) (game.getPointsPlayer2() + 1));
            }

            boolean anyTeamWonSet = setsTeamRead != game.getSetsPlayer1() || setsTeamBlue != game.getSetsPlayer2();
            if (anyTeamWonSet) {
                // Not working yet correctly...
//                gameSession.setRedTeamPlayerStartY(gameSession.getRedTeamPlayerStartY() * (-1));
//                gameSession.setBlueTeamPlayerStartY(gameSession.getBlueTeamPlayerStartY() * (-1));
            }

            List<RoomPlayer> roomPlayerList = connection.getClient().getActiveRoom().getRoomPlayerList();
            List<Client> clients = connection.getClient().getActiveGameSession().getClients();
            for (Client client : clients) {
                RoomPlayer rp = roomPlayerList.stream()
                        .filter(x -> x.getPlayer().getId().equals(client.getActivePlayer().getId()))
                        .findFirst().orElse(null);
                if (rp == null) {
                    continue;
                }

                boolean isRedTeam = rp.getPosition() == 0 || rp.getPosition() == 2;
                if (isRedTeam) {
                    gameSession.setRedTeamPlayerStartX(gameSession.getRedTeamPlayerStartX() * (-1));
                } else {
                    gameSession.setBlueTeamPlayerStartX(gameSession.getBlueTeamPlayerStartX() * (-1));
                }

                short winningPlayerPosition = (short) (gameSession.getLastBallHitByTeam() == GameFieldSide.RedTeam ? 0 : 1);
                S2CMatchplayTeamWinsPoint matchplayTeamWinsPoint =
                        new S2CMatchplayTeamWinsPoint(winningPlayerPosition, false, game.getPointsPlayer1(), game.getPointsPlayer2());
                packetEventHandler.push(createPacketEvent(client, matchplayTeamWinsPoint, PacketEventType.DEFAULT, 0), PacketEventHandler.ServerClient.SERVER);

                if (anyTeamWonSet) {
                    S2CMatchplayTeamWinsSet matchplayTeamWinsSet = new S2CMatchplayTeamWinsSet(game.getSetsPlayer1(), game.getSetsPlayer2());
                    packetEventHandler.push(createPacketEvent(client, matchplayTeamWinsSet, PacketEventType.DEFAULT, 0), PacketEventHandler.ServerClient.SERVER);
                }

                boolean madePoint = isRedTeam && gameSession.getLastBallHitByTeam() == GameFieldSide.RedTeam ||
                        !isRedTeam && gameSession.getLastBallHitByTeam() == GameFieldSide.BlueTeam;
                float playerStartX = isRedTeam ? gameSession.getRedTeamPlayerStartX() : gameSession.getBlueTeamPlayerStartX();
                float playerStartY = isRedTeam ? gameSession.getRedTeamPlayerStartY() : gameSession.getBlueTeamPlayerStartY();
                S2CMatchplayTriggerServe matchplayTriggerServe = new S2CMatchplayTriggerServe(rp.getPosition(), playerStartX, playerStartY, madePoint);

                packetEventHandler.push(createPacketEvent(client, matchplayTriggerServe, PacketEventType.FIRE_DELAYED, TimeUnit.SECONDS.toMillis(8)), PacketEventHandler.ServerClient.SERVER);
            }

            gameSession.setTimeLastBallWasHit(-1);
            gameSession.setLastBallHitByTeam(-1);
        }
    }

    private void handleQueuedPackets() {
        long currentTime = Instant.now().toEpochMilli();

        // handle client packets in queue
        PacketEvent clientPacketEvent = packetEventHandler.getClient_packetEventList().stream()
                .filter(packetEvent -> packetEvent.getPacket().getPacketId() == PacketID.C2CBallAnimationPacket && packetEvent.shouldFire(currentTime))
                .reduce((first, second) -> second)
                .orElse(null);
        if (clientPacketEvent != null) {
            handleGameSessionState(clientPacketEvent, currentTime);
            int index = packetEventHandler.getClient_packetEventList().indexOf(clientPacketEvent);

            // clear all client packets from the event list up to the last index
            for (int i = index; i >= 0; i--) {
                packetEventHandler.remove(i, PacketEventHandler.ServerClient.CLIENT);
                --index;
            }
        }

        // handle server packets in queue
        for (int i = 0; i < packetEventHandler.getServer_packetEventList().size(); i++) {
            PacketEvent packetEvent = packetEventHandler.getServer_packetEventList().get(i);
            if (!packetEvent.isFired() && packetEvent.shouldFire(currentTime)) {
                packetEvent.fire();
                packetEventHandler.remove(i, PacketEventHandler.ServerClient.SERVER);
            }
        }
    }

    private PacketEvent createPacketEvent(Client client, Packet packet, PacketEventType packetEventType, long eventFireTime) {
        long packetTimestamp = Instant.now().toEpochMilli();

        PacketEvent packetEvent = new PacketEvent();
        packetEvent.setSender(client.getConnection());
        packetEvent.setClient(client);
        packetEvent.setPacket(packet);
        packetEvent.setPacketTimestamp(packetTimestamp);
        packetEvent.setPacketEventType(packetEventType);
        packetEvent.setEventFireTime(eventFireTime);

        return packetEvent;
    }

    private static Rectangle getGameFieldRectangle() {
        return new Rectangle(new Point(-6300, -12500), new Dimension(12600,25000));
    }
}
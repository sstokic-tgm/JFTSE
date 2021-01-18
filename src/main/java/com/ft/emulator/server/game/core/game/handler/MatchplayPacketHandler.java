package com.ft.emulator.server.game.core.game.handler;

import com.ft.emulator.server.game.core.constants.GameFieldSide;
import com.ft.emulator.server.game.core.matchplay.room.GameSession;
import com.ft.emulator.server.game.core.matchplay.room.Room;
import com.ft.emulator.server.game.core.matchplay.room.RoomPlayer;
import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.game.core.packet.packets.S2CWelcomePacket;
import com.ft.emulator.server.game.core.packet.packets.matchplay.C2SMatchplayPlayerIdsInSessionPacket;
import com.ft.emulator.server.game.core.packet.packets.matchplay.S2CMatchplayTriggerServe;
import com.ft.emulator.server.game.core.packet.packets.matchplay.relay.C2CBallAnimationPacket;
import com.ft.emulator.server.game.core.packet.packets.matchplay.relay.C2CPlayerAnimationPacket;
import com.ft.emulator.server.game.core.service.PlayerService;
import com.ft.emulator.server.networking.Connection;
import com.ft.emulator.server.networking.packet.Packet;
import com.ft.emulator.server.shared.module.Client;
import com.ft.emulator.server.shared.module.GameHandler;
import com.ft.emulator.server.shared.module.RelayHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Log4j2
public class MatchplayPacketHandler {
    private final RelayHandler relayHandler;
    private final PlayerService playerService;
    private final GameHandler gameHandler;

    public RelayHandler getRelayHandler() {
        return relayHandler;
    }

    public void sendWelcomePacket(Connection connection) {
        S2CWelcomePacket welcomePacket = new S2CWelcomePacket(0, 0, 0, 0);
        connection.sendTCP(welcomePacket);
    }

    public void handleRelayPacketToClientsInGameSessionRequest(Connection connection, Packet packet) {
        Packet relayPacket = new Packet(packet.getData());
        switch (relayPacket.getPacketId()) {
            case PacketID.C2CBallAnimationPacket:
                C2CBallAnimationPacket ballAnimationPacket = new C2CBallAnimationPacket(packet);
                GameSession gameSession = connection.getClient().getActiveGameSession();
                gameSession.setTimeLastBallWasHit(System.currentTimeMillis());
                gameSession.setLastBallHitByTeam(ballAnimationPacket.getPlayerPosition());
                break;
            case PacketID.C2CPlayerAnimationPacket:
                C2CPlayerAnimationPacket playerAnimationPacket = new C2CPlayerAnimationPacket(packet);
                break;
        }

        handleGameSessionState(connection);
        sendPacketToAllClientInSameGameSession(connection, relayPacket);
    }

    public void handleRegisterPlayerForSession(Connection connection, Packet packet) {
        C2SMatchplayPlayerIdsInSessionPacket matchplayPlayerIdsInSessionPacket = new C2SMatchplayPlayerIdsInSessionPacket(packet);
        int sessionId = matchplayPlayerIdsInSessionPacket.getPlayerIds().hashCode();
        GameSession existingGameSession = this.getRelayHandler().getSessionList().stream()
                .filter(x -> x.getSessionId() == sessionId).findFirst().orElse(null);

        if (existingGameSession != null) {
            existingGameSession.getClients().add(connection.getClient());
            connection.getClient().setActiveGameSession(existingGameSession);
        }
        else {
            GameSession gameSession = new GameSession();
            gameSession.setSessionId(sessionId);
            int playerId = matchplayPlayerIdsInSessionPacket.getPlayerIds().stream().findFirst().orElse(-1);
            Client playerCLient = gameHandler.getClientOfPlayer(playerId);
            gameSession.setRoom(playerCLient.getActiveRoom());
            gameSession.getClients().add(connection.getClient());
            connection.getClient().setActiveGameSession(gameSession);
            this.relayHandler.getSessionList().add(gameSession);
        }

        Packet answer = new Packet(PacketID.S2CMatchplayAckPlayerInformation);
        answer.write((byte) 0);
        connection.sendTCP(answer);
    }

    public void handleDisconnect(Connection connection) {
        Client client = connection.getClient();
        GameSession gameSession = client.getActiveGameSession();
        if (gameSession == null) return;

        client.setActiveGameSession(null);
        gameSession.getClients().removeIf(x -> x.getConnection().getId() == connection.getId());
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
            client.getConnection().sendTCP(packet);
        }
    }

    private void handleGameSessionState(Connection connection) {
        GameSession gameSession = connection.getClient().getActiveGameSession();
        if (gameSession == null) return;
        if (gameSession.getTimeLastBallWasHit() == -1) return;
        if (gameSession.getLastBallHitByTeam() == -1) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - gameSession.getTimeLastBallWasHit() > TimeUnit.SECONDS.toMillis(3))
        {
            Room room = gameSession.getRoom();
            List<RoomPlayer> roomPlayerList = room.getRoomPlayerList();
            List<Client> clients = this.gameHandler.getClientsInRoom(room.getRoomId());
            for (Client client : clients){
                RoomPlayer rp = roomPlayerList.stream()
                        .filter(x -> x.getPlayer().getId().equals(client.getActivePlayer().getId()))
                        .findFirst().orElse(null);
                if (rp == null) {
                    continue;
                }

                boolean isRedTeam = rp.getPosition() == 0 || rp.getPosition() == 2;
                boolean shouldServe = isRedTeam && gameSession.getLastBallHitByTeam() == GameFieldSide.RedTeam ||
                        !isRedTeam && gameSession.getLastBallHitByTeam() == GameFieldSide.BlueTeam;

                float playerStartX;
                float playerStartY = isRedTeam ? -120f : 120f;
                if (isRedTeam) {
                    playerStartX = gameSession.getLastRedTeamPlayerStartX() * (-1);
                    gameSession.setLastRedTeamPlayerStartX(playerStartX);
                }
                else {
                    playerStartX = gameSession.getLastBlueTeamPlayerStartX() * (-1);
                    gameSession.setLastBlueTeamPlayerStartX(playerStartX);
                }

                S2CMatchplayTriggerServe matchplayTriggerServe =
                        new S2CMatchplayTriggerServe(rp, playerStartX, playerStartY, shouldServe);
                client.getConnection().sendTCP(matchplayTriggerServe);
            }

            gameSession.setTimeLastBallWasHit(-1);
            gameSession.setLastBallHitByTeam(-1);
        }
    }

    private static Rectangle getGameFieldRectangle() {
        return new Rectangle(new Point(-6300, -12500), new Dimension(12600,25000));
    }
}
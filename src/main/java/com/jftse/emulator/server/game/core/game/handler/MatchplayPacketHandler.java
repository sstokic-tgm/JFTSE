package com.jftse.emulator.server.game.core.game.handler;

import com.jftse.emulator.server.game.core.matchplay.GameSessionManager;
import com.jftse.emulator.server.game.core.matchplay.room.GameSession;
import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.game.core.packet.packets.S2CWelcomePacket;
import com.jftse.emulator.server.game.core.packet.packets.matchplay.S2CMatchplayBackToRoom;
import com.jftse.emulator.server.game.core.service.PlayerStatisticService;
import com.jftse.emulator.server.networking.Connection;
import com.jftse.emulator.server.networking.packet.Packet;
import com.jftse.emulator.server.shared.module.Client;
import com.jftse.emulator.server.shared.module.RelayHandler;
import com.jftse.emulator.server.game.core.packet.packets.matchplay.C2SMatchplayPlayerIdsInSessionPacket;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class MatchplayPacketHandler {
    private final GameSessionManager gameSessionManager;
    private final RelayHandler relayHandler;
    private final PlayerStatisticService playerStatisticService;

    @PostConstruct
    public void init() {
    }

    public RelayHandler getRelayHandler() {
        return relayHandler;
    }

    public void handleCleanUp() {
        this.relayHandler.getClientList().clear();
        gameSessionManager.getGameSessionList().clear();
    }

    public void sendWelcomePacket(Connection connection) {
        S2CWelcomePacket welcomePacket = new S2CWelcomePacket(0, 0, 0, 0);
        connection.sendTCP(welcomePacket);
    }

    public void handleRelayPacketToClientsInGameSessionRequest(Connection connection, Packet packet) {
        Packet relayPacket = new Packet(packet.getData());
        sendPacketToAllClientInSameGameSession(connection, relayPacket);
    }

    public void handleRegisterPlayerForSession(Connection connection, Packet packet) {
        C2SMatchplayPlayerIdsInSessionPacket matchplayPlayerIdsInSessionPacket = new C2SMatchplayPlayerIdsInSessionPacket(packet);

        int playerId = matchplayPlayerIdsInSessionPacket.getPlayerIds().stream().findFirst().orElse(-1);
        int sessionId = matchplayPlayerIdsInSessionPacket.getSessionId();

        GameSession gameSession = this.gameSessionManager.getGameSessionBySessionId(sessionId);
        if (gameSession != null) {
            if (playerId != -1) {
                Client playerClient = gameSession.getClientByPlayerId(playerId);
                Client client = new Client();
                client.setActiveRoom(playerClient.getActiveRoom());
                client.setActivePlayer(playerClient.getActivePlayer());
                client.setActiveGameSession(gameSession);
                client.setConnection(connection);

                connection.setClient(client);
                gameSession.getClientsInRelay().add(client);
                this.relayHandler.addClient(client);

                Packet answer = new Packet(PacketID.S2CMatchplayAckPlayerInformation);
                answer.write((byte) 0);
                connection.sendTCP(answer);
            }
        }
        else {
            connection.close();
        }
    }

    public void handleDisconnected(Connection connection) {
        if (connection.getClient() == null) {
            connection.close();
            return;
        }
        GameSession gameSession = connection.getClient().getActiveGameSession();
        if (gameSession != null) {
            List<Client> clientsInGameSession = gameSession.getClients();
            for (Client client : clientsInGameSession) {
                S2CMatchplayBackToRoom backToRoomPacket = new S2CMatchplayBackToRoom();
                if (client.getConnection() != null && client.getConnection().isConnected())
                    client.getConnection().sendTCP(backToRoomPacket);
            }
            List<Client> relayClients = this.relayHandler.getClientsInGameSession(gameSession.getSessionId());
            for (Client client : relayClients) {
                if (client.getConnection() != null && client.getConnection().isConnected() && client.getConnection().getId() != connection.getId()) {
                    this.relayHandler.removeClient(client);
                    client.getConnection().close();
                }
            }
        }
        this.relayHandler.removeClient(connection.getClient());
        connection.close();
    }

    public void handleUnknown(Connection connection, Packet packet) {
        Packet unknownAnswer = new Packet((char) (packet.getPacketId() + 1));
        unknownAnswer.write((short) 0);
        connection.sendTCP(unknownAnswer);
    }

    private void sendPacketToAllClientInSameGameSession(Connection connection, Packet packet) {
        if (connection.getClient() != null) {
            GameSession gameSession = connection.getClient().getActiveGameSession();
            if (gameSession != null) {
                List<Client> clientList = this.relayHandler.getClientsInGameSession(gameSession.getSessionId());
                for (Client client : clientList) {
                    if (client.getConnection() != null && client.getConnection().isConnected()) {
                        client.getConnection().sendTCP(packet);
                    }
                }
            }
        }
    }
}
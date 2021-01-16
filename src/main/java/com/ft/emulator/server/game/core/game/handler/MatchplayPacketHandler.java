package com.ft.emulator.server.game.core.game.handler;

import com.ft.emulator.server.database.model.player.Player;
import com.ft.emulator.server.game.core.matchplay.room.GameSession;
import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.game.core.packet.packets.S2CWelcomePacket;
import com.ft.emulator.server.game.core.packet.packets.matchplay.C2SMatchplayPlayerIdsInSessionPacket;
import com.ft.emulator.server.game.core.service.PlayerService;
import com.ft.emulator.server.networking.Connection;
import com.ft.emulator.server.networking.packet.Packet;
import com.ft.emulator.server.shared.module.Client;
import com.ft.emulator.server.shared.module.RelayHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class MatchplayPacketHandler {
    private final RelayHandler relayHandler;

    private final PlayerService playerService;

    public RelayHandler getRelayHandler() {
        return relayHandler;
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
            for (int playerId : matchplayPlayerIdsInSessionPacket.getPlayerIds()) {
                Player player = this.playerService.findById((long) playerId);
                gameSession.getSessionPlayers().add(player);
            }

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
        List<Client> clientList = relayHandler.getClientsInGameSession(connection.getClient());
        for (Client client : clientList) {
            if (client.getConnection().getId() == connection.getId()) {
                continue;
            }

            client.getConnection().sendTCP(packet);
        }
    }
}

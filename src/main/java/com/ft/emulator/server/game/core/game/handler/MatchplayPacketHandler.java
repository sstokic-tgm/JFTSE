package com.ft.emulator.server.game.core.game.handler;

import com.ft.emulator.server.database.model.player.Player;
import com.ft.emulator.server.game.core.constants.RoomStatus;
import com.ft.emulator.server.game.core.matchplay.GameSessionManager;
import com.ft.emulator.server.game.core.matchplay.event.PacketEventHandler;
import com.ft.emulator.server.game.core.matchplay.room.GameSession;
import com.ft.emulator.server.game.core.matchplay.room.Room;
import com.ft.emulator.server.game.core.matchplay.room.RoomPlayer;
import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.game.core.packet.packets.S2CDisconnectAnswerPacket;
import com.ft.emulator.server.game.core.packet.packets.S2CWelcomePacket;
import com.ft.emulator.server.game.core.packet.packets.matchplay.*;
import com.ft.emulator.server.game.core.service.PlayerStatisticService;
import com.ft.emulator.server.networking.Connection;
import com.ft.emulator.server.networking.packet.Packet;
import com.ft.emulator.server.shared.module.Client;
import com.ft.emulator.server.shared.module.RelayHandler;
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
            Client playerClient = gameSession.getClientByPlayerId(playerId);
            int playerClientIndex = gameSession.getClients().indexOf(playerClient);
            Client client = new Client();
            client.setActiveRoom(playerClient.getActiveRoom());
            client.setActivePlayer(playerClient.getActivePlayer());
            client.setActiveGameSession(gameSession);
            client.setConnection(playerClient.getConnection());
            client.setRelayConnection(connection);
            gameSession.getClients().set(playerClientIndex, client);

            connection.setClient(client);
            this.relayHandler.addClient(client);

            Packet answer = new Packet(PacketID.S2CMatchplayAckPlayerInformation);
            answer.write((byte) 0);
            connection.sendTCP(answer);
        }
        else {
            // disconnect all clients maybe? put them back to the room maybe?
        }
    }

    public void handleDisconnected(Connection connection) {
        Client client = connection.getClient();
        if (client == null) return;

        GameSession gameSession = client.getActiveGameSession();
        if (gameSession == null) { // server checkers and other, we need to remove the client from relay handler otherwise floating connections
            connection.setClient(null);
            this.relayHandler.removeClient(connection.getClient());

            connection.close();
        }
        else {
            Room currentClientRoom = connection.getClient().getActiveRoom();
            Player player = connection.getClient().getActivePlayer();
            if (player != null && currentClientRoom != null && currentClientRoom.getStatus() == RoomStatus.Running) {
                player.getPlayerStatistic().setNumberOfDisconnects(player.getPlayerStatistic().getNumberOfDisconnects() + 1);
                playerStatisticService.save(player.getPlayerStatistic());
            }

            gameSession.getClients().forEach(c -> {
                Room room = c.getActiveRoom();
                room.setStatus(RoomStatus.NotRunning);
                room.getRoomPlayerList().forEach(x -> x.setReady(false));
                c.setActiveRoom(room);

                RoomPlayer roomPlayer = room.getRoomPlayerList().stream()
                        .filter(rp -> rp.getPosition() == 0 && rp.getPlayer().getId().equals(c.getActivePlayer().getId()))
                        .findAny()
                        .orElse(null);

                S2CMatchplayBackToRoom backToRoomPacket = new S2CMatchplayBackToRoom();
                if (c.getRelayConnection() != null && c.getConnection() != null && c.getRelayConnection().getId() != connection.getId())
                    c.getConnection().sendTCP(backToRoomPacket);

                if (roomPlayer != null && c.getRelayConnection().getId() != connection.getId()) {
                    Packet unsetHostPacket = new Packet(PacketID.S2CUnsetHost);
                    unsetHostPacket.write((byte) 0);
                    c.getConnection().sendTCP(unsetHostPacket);
                }

                c.setActiveGameSession(null);

                c.getRelayConnection().setClient(null);
                this.relayHandler.removeClient(c);

                c.getRelayConnection().close();
            });

            gameSession.getClients().clear();
            gameSessionManager.removeGameSession(gameSession);
        }
    }

    public void handleUnknown(Connection connection, Packet packet) {
        Packet unknownAnswer = new Packet((char) (packet.getPacketId() + 1));
        unknownAnswer.write((short) 0);
        connection.sendTCP(unknownAnswer);
    }

    private void sendPacketToAllClientInSameGameSession(Connection connection, Packet packet) {
        GameSession gameSession = connection.getClient().getActiveGameSession();
        if (gameSession != null) {
            List<Client> clientList = relayHandler.getClientsInGameSession(gameSession.getSessionId());
            for (Client client : clientList) {
                client.getRelayConnection().sendTCP(packet);
            }
        }
    }
}
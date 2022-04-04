package com.jftse.emulator.server.core.handler.relay;

import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.core.constants.RoomStatus;
import com.jftse.emulator.server.core.manager.RelayManager;
import com.jftse.emulator.server.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.core.matchplay.game.MatchplayBattleGame;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.core.matchplay.battle.PlayerBattleState;
import com.jftse.emulator.server.core.matchplay.room.GameSession;
import com.jftse.emulator.server.core.matchplay.room.Room;
import com.jftse.emulator.server.core.matchplay.room.RoomPlayer;
import com.jftse.emulator.server.core.packet.packets.S2CWelcomePacket;
import com.jftse.emulator.server.core.packet.packets.matchplay.S2CMatchplayBackToRoom;
import com.jftse.emulator.server.networking.Connection;
import com.jftse.emulator.server.shared.module.Client;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

@Log4j2
public class BasicRelayHandler {

    public BasicRelayHandler() {

    }

    public void sendWelcomePacket(Connection connection) {
        S2CWelcomePacket welcomePacket = new S2CWelcomePacket(connection.getDecKey(), connection.getEncKey(), 0, 0);
        connection.sendTCP(welcomePacket);
    }

    public void handleDisconnected(Connection connection) {
        if (connection.getClient() == null)
            return;

        boolean notifyClients = !connection.getClient().isSpectator();
        GameSession gameSession = connection.getClient().getActiveGameSession();
        if (gameSession != null && notifyClients) {
            List<Client> clientsInGameSession = new ArrayList<>(gameSession.getClients());
            for (Client client : clientsInGameSession) {
                S2CMatchplayBackToRoom backToRoomPacket = new S2CMatchplayBackToRoom();
                if (client.getConnection() != null && client.getConnection().isConnected())
                    client.getConnection().sendTCP(backToRoomPacket);
            }
            ArrayList<Client> relayClients = RelayManager.getInstance().getClientsInGameSession(gameSession.getSessionId());
            for (Client client : relayClients) {
                if (client.getConnection() != null && client.getConnection().isConnected() && client.getConnection().getId() != connection.getId()) {
                    RelayManager.getInstance().removeClient(client);
                    client.getConnection().close();
                }
            }
        }
        RelayManager.getInstance().removeClient(connection.getClient());
        connection.setClient(null);
    }

    public void handleTimeout(Connection connection) {
        Client client = connection.getClient();
        if (client == null) {
            connection.close();
            return;
        }

        Room room = client.getActiveRoom();
        if (room == null || room.getStatus() != RoomStatus.Running) {
            if (room != null) {
                // Test if on timeout an active game session exist for the timing out client
                GameSession gameSession = client.getActiveGameSession();
                if (gameSession != null) {
                    boolean allClientsInactiveGameSession = gameSession.getClients().stream().allMatch(c -> c.getActiveGameSession() == null);
                    if (!allClientsInactiveGameSession)
                        return;
                }

                log.warn(String.format("Room  state is %s . Close connection", room.getStatus()));
            }

            connection.close();
            return;
        }

        GameSession gameSession = client.getActiveGameSession();
        if (gameSession == null) {
            connection.close();
            return;
        }

        MatchplayGame game = gameSession.getActiveMatchplayGame();
        if (game == null) {
            connection.close();
            return;
        }

        if (room.getStatus() == RoomStatus.Running && client.isSpectator()) {
            return;
        }

        if (game instanceof MatchplayGuardianGame || game instanceof MatchplayBattleGame) {
            if (room.getStatus() == RoomStatus.Running) {
                // Test if people won't back thrown back to room during guardian game if we do it this way.
                // If no bug reports come anymore delete the tryHandleTimeoutForGuardianGameMatch method.
                return;
            }

            boolean success = this.tryHandleTimeoutForGuardianGameMatch(connection, client, room, (MatchplayGuardianGame) game);
            if (success) {
                return;
            }
        }

        connection.close();
    }

    private boolean tryHandleTimeoutForGuardianGameMatch(Connection connection, Client client, Room room, MatchplayGuardianGame game) {
        RoomPlayer roomPlayer = client.getRoomPlayer();
        if (roomPlayer == null) return false;

        PlayerBattleState playerBattleState = game.getPlayerBattleStates().stream()
                .filter(x -> x.getPosition() == roomPlayer.getPosition())
                .findFirst()
                .orElse(null);
        if (playerBattleState == null) return false;

        if (playerBattleState.isDead()) {
            connection.getTcpConnection().setLastReadTime(System.currentTimeMillis());
            return true;
        }

        return false;
    }
}

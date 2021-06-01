package com.jftse.emulator.server.game.core.game.handler;

import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.game.core.constants.RoomStatus;
import com.jftse.emulator.server.game.core.matchplay.GameSessionManager;
import com.jftse.emulator.server.game.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.game.core.matchplay.basic.MatchplayBasicGame;
import com.jftse.emulator.server.game.core.matchplay.basic.MatchplayBattleGame;
import com.jftse.emulator.server.game.core.matchplay.basic.MatchplayGuardianGame;
import com.jftse.emulator.server.game.core.matchplay.battle.PlayerBattleState;
import com.jftse.emulator.server.game.core.matchplay.room.GameSession;
import com.jftse.emulator.server.game.core.matchplay.room.Room;
import com.jftse.emulator.server.game.core.matchplay.room.RoomPlayer;
import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.game.core.packet.packets.S2CWelcomePacket;
import com.jftse.emulator.server.game.core.packet.packets.chat.S2CChatRoomAnswerPacket;
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
import java.util.ArrayList;
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
                if (playerClient != null) {
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
                } else {
                    Packet answer = new Packet(PacketID.S2CMatchplayAckPlayerInformation);
                    answer.write((byte) 1);
                    connection.sendTCP(answer);

                    List<Client> clientsInGameSession = new ArrayList<>();
                    clientsInGameSession.addAll(gameSession.getClients()); // deep copy
                    for (Client client : clientsInGameSession) {
                        client.getActiveRoom().setStatus(RoomStatus.StartCancelled);
                    }
                    for (Client client : clientsInGameSession) {
                        Room room = client.getActiveRoom();
                        if (room != null) {
                            RoomPlayer roomPlayer = room.getRoomPlayerList().stream()
                                    .filter(rp -> rp.getPlayer().getId() == playerId)
                                    .findAny()
                                    .orElse(null);
                            if (roomPlayer != null) {
                                String message = roomPlayer.getPlayer().getName() + " has to relog.";
                                S2CChatRoomAnswerPacket chatRoomAnswerPacket = new S2CChatRoomAnswerPacket((byte) 2, "Room", message);
                                clientsInGameSession.forEach(c -> c.getConnection().sendTCP(chatRoomAnswerPacket));
                                break;
                            }
                        }
                    }
                    List<Client> relayClients = this.relayHandler.getClientsInGameSession(gameSession.getSessionId());
                    for (Client client : relayClients) {
                        if (client.getConnection() != null && client.getConnection().isConnected()) {
                            this.relayHandler.removeClient(client);
                            client.getConnection().close();
                        }
                    }

                    log.warn("Couldn't find client for player. Cancel connection to relay server");
                    this.relayHandler.removeClient(connection.getClient());
                    this.gameSessionManager.removeGameSession(gameSession);
                    connection.close();
                }
            }
        } else {
            Packet answer = new Packet(PacketID.S2CMatchplayAckPlayerInformation);
            answer.write((byte) 1);
            connection.sendTCP(answer);

            log.warn("Couldn't find gamesession. Cancel connection to relay server");
            this.relayHandler.removeClient(connection.getClient());
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
            List<Client> clientsInGameSession = new ArrayList<>();
            clientsInGameSession.addAll(gameSession.getClients()); // deep copy
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

    public void handleTimeout(Connection connection) {
        Client client = connection.getClient();
        if (client == null) {
            connection.close();
            return;
        }

        Room room = client.getActiveRoom();
        if (room == null || room.getStatus() != RoomStatus.Running) {
            if (room != null) {
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
        Player player = client.getActivePlayer();
        if (player == null) return false;

        RoomPlayer roomPlayer = room.getRoomPlayerList().stream()
                .filter(x -> x.getPlayer() == player)
                .findFirst()
                .orElse(null);
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
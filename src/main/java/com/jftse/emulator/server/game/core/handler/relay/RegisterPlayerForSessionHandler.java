package com.jftse.emulator.server.game.core.handler.relay;

import com.jftse.emulator.server.game.core.constants.RoomStatus;
import com.jftse.emulator.server.game.core.handler.AbstractHandler;
import com.jftse.emulator.server.game.core.manager.RelayManager;
import com.jftse.emulator.server.game.core.matchplay.GameSessionManager;
import com.jftse.emulator.server.game.core.matchplay.room.GameSession;
import com.jftse.emulator.server.game.core.matchplay.room.Room;
import com.jftse.emulator.server.game.core.matchplay.room.RoomPlayer;
import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.game.core.packet.packets.chat.S2CChatRoomAnswerPacket;
import com.jftse.emulator.server.game.core.packet.packets.matchplay.C2SMatchplayPlayerIdsInSessionPacket;
import com.jftse.emulator.server.networking.packet.Packet;
import com.jftse.emulator.server.shared.module.Client;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

@Log4j2
public class RegisterPlayerForSessionHandler extends AbstractHandler {
    private C2SMatchplayPlayerIdsInSessionPacket matchplayPlayerIdsInSessionPacket;

    @Override
    public boolean process(Packet packet) {
        matchplayPlayerIdsInSessionPacket = new C2SMatchplayPlayerIdsInSessionPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        int playerId = matchplayPlayerIdsInSessionPacket.getPlayerIds().stream().findFirst().orElse(-1);
        int sessionId = matchplayPlayerIdsInSessionPacket.getSessionId();

        GameSession gameSession = GameSessionManager.getInstance().getGameSessionBySessionId(sessionId);
        if (gameSession != null) {
            if (playerId != -1) {
                Client playerClient = gameSession.getClientByPlayerId(playerId);
                if (playerClient != null) {
                    Client client = new Client();
                    client.setActiveRoom(playerClient.getActiveRoom());
                    client.setActivePlayer(playerClient.getActivePlayer());
                    client.setActiveGameSession(gameSession);
                    client.setConnection(connection);
                    client.setSpectator(matchplayPlayerIdsInSessionPacket.isSpectator());

                    connection.setClient(client);
                    gameSession.getClientsInRelay().add(client);
                    RelayManager.getInstance().addClient(client);

                    Packet answer = new Packet(PacketID.S2CMatchplayAckPlayerInformation);
                    answer.write((byte) 0);
                    connection.sendTCP(answer);
                } else {
                    Packet answer = new Packet(PacketID.S2CMatchplayAckPlayerInformation);
                    answer.write((byte) 1);
                    connection.sendTCP(answer);

                    List<Client> clientsInGameSession = new ArrayList<>(gameSession.getClients());
                    for (Client client : clientsInGameSession) {
                        if (client.getActiveRoom() != null)
                            client.getActiveRoom().setStatus(RoomStatus.StartCancelled);
                        client.setActiveGameSession(null);
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
                                clientsInGameSession.forEach(c -> {
                                    if (c.getConnection() != null && c.getConnection().isConnected())
                                        c.getConnection().sendTCP(chatRoomAnswerPacket);
                                });
                                break;
                            }
                        }
                    }
                    ConcurrentLinkedDeque<Client> relayClients = RelayManager.getInstance().getClientsInGameSession(gameSession.getSessionId());
                    for (Client client : relayClients) {
                        if (client.getConnection() != null && client.getConnection().isConnected()) {
                            RelayManager.getInstance().removeClient(client);
                            client.getConnection().close();
                        }
                    }

                    log.warn("Couldn't find client for player. Cancel connection to relay server");
                    RelayManager.getInstance().removeClient(connection.getClient());
                    GameSessionManager.getInstance().removeGameSession(gameSession);
                    connection.close();
                }
            }
        } else {
            Packet answer = new Packet(PacketID.S2CMatchplayAckPlayerInformation);
            answer.write((byte) 1);
            connection.sendTCP(answer);

            log.warn("Couldn't find gamesession. Cancel connection to relay server");
            RelayManager.getInstance().removeClient(connection.getClient());
            connection.close();
        }
    }
}

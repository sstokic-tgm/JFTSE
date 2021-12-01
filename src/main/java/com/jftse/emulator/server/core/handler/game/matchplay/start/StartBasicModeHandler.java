package com.jftse.emulator.server.core.handler.game.matchplay.start;

import com.jftse.emulator.server.core.constants.ServeType;
import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.matchplay.game.MatchplayBasicGame;
import com.jftse.emulator.server.core.matchplay.room.GameSession;
import com.jftse.emulator.server.core.matchplay.room.Room;
import com.jftse.emulator.server.core.matchplay.room.RoomPlayer;
import com.jftse.emulator.server.core.matchplay.room.ServeInfo;
import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.core.packet.packets.matchplay.S2CMatchplayTriggerServe;
import com.jftse.emulator.server.networking.packet.Packet;
import com.jftse.emulator.server.shared.module.Client;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class StartBasicModeHandler extends AbstractHandler {
    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        Packet removeBlackBarsPacket = new Packet(PacketOperations.S2CGameRemoveBlackBars.getValueAsChar());
        GameManager.getInstance().sendPacketToAllClientsInSameGameSession(removeBlackBarsPacket, connection);

        Room room = connection.getClient().getActiveRoom();
        ConcurrentLinkedDeque<Client> clients = new ConcurrentLinkedDeque<>(connection.getClient().getActiveGameSession().getClients());
        List<ServeInfo> serveInfo = new ArrayList<>();
        int clientsSize = clients.size();
        for (int i = 0; i < clientsSize; i++) {
            Client client = clients.poll();

            RoomPlayer rp = room.getRoomPlayerList().stream()
                    .filter(x -> x.getPlayer().getId().equals(client.getActivePlayer().getId()))
                    .findFirst()
                    .orElse(null);

            if (rp != null) {
                boolean isActivePlayer = rp.getPosition() < 4;
                if (isActivePlayer) {
                    GameSession gameSession = client.getActiveGameSession();
                    MatchplayBasicGame game = (MatchplayBasicGame) gameSession.getActiveMatchplayGame();

                    Point playerLocation = new Point();
                    int playerLocationsOnMapSize = game.getPlayerLocationsOnMap().size();
                    for (int j = 0; j < playerLocationsOnMapSize; j++) {
                        Point current = game.getPlayerLocationsOnMap().poll();

                        if (rp.getPosition() == j)
                            playerLocation = new Point(current);

                        game.getPlayerLocationsOnMap().offer(current);
                    }

                    byte serveType = ServeType.None;
                    if (rp.getPosition() == 0) {
                        serveType = ServeType.ServeBall;
                        game.getServePlayer().getAndSet(rp);
                    }
                    if (rp.getPosition() == 1) {
                        serveType = ServeType.ReceiveBall;
                        game.getReceiverPlayer().getAndSet(rp);
                    }
                    ServeInfo playerServeInfo = new ServeInfo();
                    playerServeInfo.setPlayerPosition(rp.getPosition());
                    playerServeInfo.setPlayerStartLocation(playerLocation);
                    playerServeInfo.setServeType(serveType);
                    serveInfo.add(playerServeInfo);
                }
            }
        }
        S2CMatchplayTriggerServe matchplayTriggerServe = new S2CMatchplayTriggerServe(serveInfo);
        GameManager.getInstance().sendPacketToAllClientsInSameGameSession(matchplayTriggerServe, connection);
    }
}

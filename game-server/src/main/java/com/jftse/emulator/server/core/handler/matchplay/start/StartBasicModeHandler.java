package com.jftse.emulator.server.core.handler.matchplay.start;

import com.jftse.emulator.server.core.constants.ServeType;
import com.jftse.emulator.server.core.life.room.ServeInfo;
import com.jftse.emulator.server.core.packets.matchplay.S2CMatchplayTriggerServe;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.matchplay.game.MatchplayBasicGame;
import com.jftse.emulator.server.core.life.room.GameSession;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class StartBasicModeHandler extends AbstractPacketHandler {
    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        FTClient ftClient = (FTClient) connection.getClient();

        Packet removeBlackBarsPacket = new Packet(PacketOperations.S2CGameRemoveBlackBars.getValue());
        GameManager.getInstance().sendPacketToAllClientsInSameGameSession(removeBlackBarsPacket, ftClient.getConnection());

        List<FTClient> clients = new ArrayList<>(ftClient.getActiveGameSession().getClients());
        List<ServeInfo> serveInfo = new ArrayList<>();

        clients.forEach(client -> {
            RoomPlayer rp = client.getRoomPlayer();

            if (rp != null) {
                boolean isActivePlayer = rp.getPosition() < 4;
                if (isActivePlayer) {
                    GameSession gameSession = client.getActiveGameSession();
                    MatchplayBasicGame game = (MatchplayBasicGame) gameSession.getMatchplayGame();

                    Point playerLocation = game.getPlayerLocationsOnMap().get(rp.getPosition());

                    byte serveType = ServeType.None;
                    if (rp.getPosition() == 0) {
                        serveType = ServeType.ServeBall;
                        game.setServePlayer(rp);
                    }
                    if (rp.getPosition() == 1) {
                        serveType = ServeType.ReceiveBall;
                        game.setReceiverPlayer(rp);
                    }
                    ServeInfo playerServeInfo = new ServeInfo();
                    playerServeInfo.setPlayerPosition(rp.getPosition());
                    playerServeInfo.setPlayerStartLocation(playerLocation);
                    playerServeInfo.setServeType(serveType);
                    serveInfo.add(playerServeInfo);
                }
            }
        });

        S2CMatchplayTriggerServe matchplayTriggerServe = new S2CMatchplayTriggerServe(serveInfo);
        GameManager.getInstance().sendPacketToAllClientsInSameGameSession(matchplayTriggerServe, ftClient.getConnection());
    }
}

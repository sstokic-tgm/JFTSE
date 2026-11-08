package com.jftse.emulator.server.core.handler.matchplay;

import com.jftse.emulator.server.core.life.room.GameSession;
import com.jftse.emulator.server.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.matchplay.CMSGReportMatchPoint;

@PacketId(CMSGReportMatchPoint.PACKET_ID)
public class MatchplayPointPacketHandler implements PacketHandler<FTConnection, CMSGReportMatchPoint> {
    @Override
    public void handle(FTConnection connection, CMSGReportMatchPoint packet) {
        FTClient ftClient = connection.getClient();
        if (ftClient == null)
            return;

        if (packet.getScoringTeamSlot() < 0 || packet.getScoringTeamSlot() >= 4)
            return;

        GameSession gameSession = ftClient.getActiveGameSession();
        if (gameSession == null)
            return;

        MatchplayGame game = gameSession.getMatchplayGame();
        if (game == null)
            return;

        game.getHandleable().onPoint(ftClient, packet);
    }
}

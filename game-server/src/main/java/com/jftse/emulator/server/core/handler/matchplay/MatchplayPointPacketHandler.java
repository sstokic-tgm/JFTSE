package com.jftse.emulator.server.core.handler.matchplay;

import com.jftse.emulator.server.core.packets.matchplay.C2SMatchplayPointPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.core.life.room.GameSession;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

@PacketOperationIdentifier(PacketOperations.C2SMatchplayPoint)
public class MatchplayPointPacketHandler extends AbstractPacketHandler {
    private C2SMatchplayPointPacket matchplayPointPacket;

    @Override
    public boolean process(Packet packet) {
        this.matchplayPointPacket = new C2SMatchplayPointPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient ftClient = (FTClient) connection.getClient();
        if (ftClient == null)
            return;

        GameSession gameSession = ftClient.getActiveGameSession();
        if (gameSession == null)
            return;

        MatchplayGame game = gameSession.getMatchplayGame();
        if (game == null)
            return;

        game.getHandleable().onPoint(ftClient, matchplayPointPacket);
    }
}

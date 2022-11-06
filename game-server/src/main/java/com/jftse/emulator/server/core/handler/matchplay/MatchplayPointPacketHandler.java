package com.jftse.emulator.server.core.handler.matchplay;

import com.jftse.emulator.server.core.handler.matchplay.point.BasicModeMatchplayPointPacketHandler;
import com.jftse.emulator.server.core.handler.matchplay.point.BattleModeMatchplayPointPacketHandler;
import com.jftse.emulator.server.core.handler.matchplay.point.GuardianModeMatchplayPointPacketHandler;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.core.matchplay.game.MatchplayBasicGame;
import com.jftse.emulator.server.core.matchplay.game.MatchplayBattleGame;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.core.life.room.GameSession;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

@PacketOperationIdentifier(PacketOperations.C2SMatchplayPoint)
public class MatchplayPointPacketHandler extends AbstractPacketHandler {
    private Packet packet;

    @Override
    public boolean process(Packet packet) {
        this.packet = packet;
        return true;
    }

    @Override
    public void handle() {
        FTClient ftClient = connection.getClient();
        if (ftClient == null || ftClient.getActiveGameSession() == null || ftClient.getActiveRoom() == null)
            return;

        GameSession gameSession = ftClient.getActiveGameSession();
        AbstractPacketHandler handler;
        MatchplayGame game = gameSession.getMatchplayGame();
        if (game == null)
            return;

        if (game instanceof MatchplayBasicGame) {
            handler = new BasicModeMatchplayPointPacketHandler();
        } else if (game instanceof MatchplayBattleGame) {
            handler = new BattleModeMatchplayPointPacketHandler();
        } else if (game instanceof MatchplayGuardianGame) {
            handler = new GuardianModeMatchplayPointPacketHandler();
        } else { // default
            handler = new AbstractPacketHandler() {
                @Override
                public boolean process(Packet packet) {
                    return false;
                }

                @Override
                public void handle() {
                    // empty
                }
            };
        }

        try {
            handler.setConnection(connection);
            if (handler.process(packet))
                handler.handle();
        } catch (Exception e) {
            throw e;
        }
    }
}

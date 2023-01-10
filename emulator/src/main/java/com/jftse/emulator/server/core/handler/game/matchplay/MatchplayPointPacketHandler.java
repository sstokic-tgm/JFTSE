package com.jftse.emulator.server.core.handler.game.matchplay;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.handler.game.matchplay.point.*;
import com.jftse.emulator.server.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.core.matchplay.game.*;
import com.jftse.emulator.server.core.matchplay.room.GameSession;
import com.jftse.emulator.server.networking.packet.Packet;

public class MatchplayPointPacketHandler extends AbstractHandler {
    private Packet packet;

    @Override
    public boolean process(Packet packet) {
        this.packet = packet;
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null || connection.getClient().getActiveGameSession() == null || connection.getClient().getActiveRoom() == null)
            return;

        GameSession gameSession = connection.getClient().getActiveGameSession();
        AbstractHandler handler;
        MatchplayGame game = gameSession.getActiveMatchplayGame();
        if (game == null)
            return;

        if (game instanceof MatchplayBasicGame) {
            handler = new BasicModeMatchplayPointPacketHandler();
        } else if (game instanceof MatchplayBattleGame) {
            handler = new BattleModeMatchplayPointPacketHandler();
        } else if (game instanceof MatchplayGuardianGame) {
            handler = new GuardianModeMatchplayPointPacketHandler();
        } else { // default
            handler = new AbstractHandler() {
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
            connection.notifyException(e);
        }
    }
}

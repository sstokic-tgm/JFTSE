package com.jftse.emulator.server.core.handler.game.matchplay.point;

import com.jftse.emulator.server.core.constants.GameFieldSide;
import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.core.matchplay.room.GameSession;
import com.jftse.emulator.server.core.packet.packets.matchplay.S2CMatchplayTriggerGuardianServe;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.Random;

public class GuardianModeMatchplayPointPacketHandler extends AbstractHandler {
    private final Random random;

    public GuardianModeMatchplayPointPacketHandler() {
        random = new Random();
    }

    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null || connection.getClient().getActiveGameSession() == null)
            return;

        GameSession gameSession = connection.getClient().getActiveGameSession();
        MatchplayGuardianGame game = (MatchplayGuardianGame) gameSession.getActiveMatchplayGame();

        boolean lastGuardianServeWasOnGuardianSide = game.getLastGuardianServeSide() == GameFieldSide.Guardian;
        int servingPositionXOffset = random.nextInt(7);
        if (!lastGuardianServeWasOnGuardianSide) {
            game.setLastGuardianServeSide(GameFieldSide.Guardian);
            S2CMatchplayTriggerGuardianServe triggerGuardianServePacket = new S2CMatchplayTriggerGuardianServe(GameFieldSide.Guardian, (byte) servingPositionXOffset, (byte) 0);
            gameSession.getClients().forEach(x -> {
                x.getConnection().sendTCP(triggerGuardianServePacket);
            });
        } else {
            game.setLastGuardianServeSide(GameFieldSide.Players);
            S2CMatchplayTriggerGuardianServe triggerGuardianServePacket = new S2CMatchplayTriggerGuardianServe(GameFieldSide.Players, (byte) servingPositionXOffset, (byte) 0);
            gameSession.getClients().forEach(x -> {
                if (x.getConnection() != null && x.getConnection().isConnected()) {
                    x.getConnection().sendTCP(triggerGuardianServePacket);
                }
            });
        }
    }
}

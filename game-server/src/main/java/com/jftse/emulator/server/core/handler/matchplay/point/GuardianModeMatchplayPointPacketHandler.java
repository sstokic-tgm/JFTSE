package com.jftse.emulator.server.core.handler.matchplay.point;

import com.jftse.emulator.server.core.constants.GameFieldSide;
import com.jftse.emulator.server.core.packets.matchplay.S2CMatchplayTriggerGuardianServe;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.core.life.room.GameSession;
import com.jftse.server.core.protocol.Packet;

import java.util.Random;

public class GuardianModeMatchplayPointPacketHandler extends AbstractPacketHandler {
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
        FTClient ftClient = (FTClient) connection.getClient();
        if (ftClient == null || ftClient.getActiveGameSession() == null)
            return;

        GameSession gameSession = ftClient.getActiveGameSession();
        MatchplayGuardianGame game = (MatchplayGuardianGame) gameSession.getMatchplayGame();

        boolean lastGuardianServeWasOnGuardianSide = game.getLastGuardianServeSide() == GameFieldSide.Guardian;
        int servingPositionXOffset = random.nextInt(7);
        if (!lastGuardianServeWasOnGuardianSide) {
            game.setLastGuardianServeSide(GameFieldSide.Guardian);
            S2CMatchplayTriggerGuardianServe triggerGuardianServePacket = new S2CMatchplayTriggerGuardianServe(GameFieldSide.Guardian, (byte) servingPositionXOffset, (byte) 0);
            gameSession.getClients().forEach(x -> {
                if (x.getConnection() != null) {
                    x.getConnection().sendTCP(triggerGuardianServePacket);
                }
            });
        } else {
            game.setLastGuardianServeSide(GameFieldSide.Players);
            S2CMatchplayTriggerGuardianServe triggerGuardianServePacket = new S2CMatchplayTriggerGuardianServe(GameFieldSide.Players, (byte) servingPositionXOffset, (byte) 0);
            gameSession.getClients().forEach(x -> {
                if (x.getConnection() != null) {
                    x.getConnection().sendTCP(triggerGuardianServePacket);
                }
            });
        }
    }
}

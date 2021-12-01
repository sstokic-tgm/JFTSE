package com.jftse.emulator.server.core.handler.game.matchplay.point;

import com.jftse.emulator.server.core.constants.GameFieldSide;
import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.matchplay.game.MatchplayBattleGame;
import com.jftse.emulator.server.core.matchplay.room.GameSession;
import com.jftse.emulator.server.core.packet.packets.matchplay.S2CMatchplayTriggerGuardianServe;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.Random;

public class BattleModeMatchplayPointPacketHandler extends AbstractHandler {
    private final Random random;

    public BattleModeMatchplayPointPacketHandler() {
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
        MatchplayBattleGame game = (MatchplayBattleGame) gameSession.getActiveMatchplayGame();

        boolean lastGuardianServeWasOnBlueTeamsSide = game.getLastGuardianServeSide().get() == GameFieldSide.BlueTeam;
        int servingPositionXOffset = random.nextInt(7);
        if (!lastGuardianServeWasOnBlueTeamsSide) {
            game.getLastGuardianServeSide().getAndSet(GameFieldSide.BlueTeam);
            S2CMatchplayTriggerGuardianServe triggerGuardianServePacket = new S2CMatchplayTriggerGuardianServe((byte) GameFieldSide.BlueTeam, (byte) servingPositionXOffset, (byte) 0);
            gameSession.getClients().forEach(x -> {
                if (x.getConnection() != null && x.getConnection().isConnected()) {
                    x.getConnection().sendTCP(triggerGuardianServePacket);
                }
            });
        } else {
            game.getLastGuardianServeSide().getAndSet(GameFieldSide.RedTeam);
            S2CMatchplayTriggerGuardianServe triggerGuardianServePacket = new S2CMatchplayTriggerGuardianServe((byte) GameFieldSide.RedTeam, (byte) servingPositionXOffset, (byte) 0);
            gameSession.getClients().forEach(x -> {
                if (x.getConnection() != null && x.getConnection().isConnected()) {
                    x.getConnection().sendTCP(triggerGuardianServePacket);
                }
            });
        }
    }
}

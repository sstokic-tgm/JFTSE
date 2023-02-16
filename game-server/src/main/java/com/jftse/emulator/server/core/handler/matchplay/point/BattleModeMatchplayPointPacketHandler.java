package com.jftse.emulator.server.core.handler.matchplay.point;

import com.jftse.emulator.server.core.constants.GameFieldSide;
import com.jftse.emulator.server.core.packets.matchplay.S2CMatchplayTriggerGuardianServe;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.matchplay.game.MatchplayBattleGame;
import com.jftse.emulator.server.core.life.room.GameSession;
import com.jftse.server.core.protocol.Packet;

import java.util.Random;

public class BattleModeMatchplayPointPacketHandler extends AbstractPacketHandler {
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
        FTClient ftClient = (FTClient) connection.getClient();
        if (ftClient == null || ftClient.getActiveGameSession() == null)
            return;

        GameSession gameSession = ftClient.getActiveGameSession();
        MatchplayBattleGame game = (MatchplayBattleGame) gameSession.getMatchplayGame();

        boolean lastGuardianServeWasOnBlueTeamsSide = game.getLastGuardianServeSide() == GameFieldSide.BlueTeam;
        int servingPositionXOffset = random.nextInt(7);
        if (!lastGuardianServeWasOnBlueTeamsSide) {
            game.setLastGuardianServeSide(GameFieldSide.BlueTeam);
            S2CMatchplayTriggerGuardianServe triggerGuardianServePacket = new S2CMatchplayTriggerGuardianServe((byte) GameFieldSide.BlueTeam, (byte) servingPositionXOffset, (byte) 0);
            gameSession.getClients().forEach(x -> {
                if (x.getConnection() != null) {
                    x.getConnection().sendTCP(triggerGuardianServePacket);
                }
            });
        } else {
            game.setLastGuardianServeSide(GameFieldSide.RedTeam);
            S2CMatchplayTriggerGuardianServe triggerGuardianServePacket = new S2CMatchplayTriggerGuardianServe((byte) GameFieldSide.RedTeam, (byte) servingPositionXOffset, (byte) 0);
            gameSession.getClients().forEach(x -> {
                if (x.getConnection() != null) {
                    x.getConnection().sendTCP(triggerGuardianServePacket);
                }
            });
        }
    }
}

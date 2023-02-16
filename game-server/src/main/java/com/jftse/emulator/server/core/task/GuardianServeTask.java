package com.jftse.emulator.server.core.task;

import com.jftse.emulator.server.core.constants.GameFieldSide;
import com.jftse.emulator.server.core.life.room.GameSession;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.core.packets.matchplay.S2CGameSetNameColorAndRemoveBlackBar;
import com.jftse.emulator.server.core.packets.matchplay.S2CMatchplayTriggerGuardianServe;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.thread.AbstractTask;
import com.jftse.server.core.thread.ThreadManager;

import java.util.Random;

public class GuardianServeTask extends AbstractTask {
    private final FTConnection connection;

    private final Random random;

    public GuardianServeTask(FTConnection connection) {
        this.connection = connection;

        random = new Random();
    }

    @Override
    public void run() {
        GameSession gameSession = connection.getClient().getActiveGameSession();
        if (gameSession == null) return;
        MatchplayGuardianGame game = (MatchplayGuardianGame) gameSession.getMatchplayGame();

        int servingPositionXOffset = random.nextInt(7);
        S2CMatchplayTriggerGuardianServe triggerGuardianServePacket = new S2CMatchplayTriggerGuardianServe(GameFieldSide.Guardian, (byte) servingPositionXOffset, (byte) 0);
        S2CGameSetNameColorAndRemoveBlackBar setNameColorAndRemoveBlackBarPacket = new S2CGameSetNameColorAndRemoveBlackBar(null);
        GameManager.getInstance().sendPacketToAllClientsInSameGameSession(setNameColorAndRemoveBlackBarPacket, connection);
        GameManager.getInstance().sendPacketToAllClientsInSameGameSession(triggerGuardianServePacket, connection);
        gameSession.getClients().forEach(c -> {
            if (c != null && c.getConnection() != null) {
                c.getConnection().sendTCP(setNameColorAndRemoveBlackBarPacket);
                c.getConnection().sendTCP(triggerGuardianServePacket);
            }
        });
        game.resetStageStartTime();
        ThreadManager.getInstance().newTask(new DefeatTimerTask(connection, gameSession, game.getBossGuardianStage()));
    }
}

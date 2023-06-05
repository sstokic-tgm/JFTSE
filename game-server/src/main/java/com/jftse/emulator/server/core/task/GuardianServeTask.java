package com.jftse.emulator.server.core.task;

import com.jftse.emulator.server.core.constants.GameFieldSide;
import com.jftse.emulator.server.core.life.room.GameSession;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.core.packets.matchplay.S2CGameSetNameColorAndRemoveBlackBar;
import com.jftse.emulator.server.core.packets.matchplay.S2CMatchplayTriggerGuardianServe;
import com.jftse.emulator.server.core.utils.ServingPositionGenerator;
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

        byte servingPositionXOffset = (byte) ServingPositionGenerator.randomServingPositionXOffset();
        byte servingPositionYOffset = (byte) ServingPositionGenerator.randomServingPositionYOffset(servingPositionXOffset);

        S2CMatchplayTriggerGuardianServe triggerGuardianServePacket = new S2CMatchplayTriggerGuardianServe(GameFieldSide.Guardian, servingPositionXOffset, servingPositionYOffset);
        S2CGameSetNameColorAndRemoveBlackBar setNameColorAndRemoveBlackBarPacket = new S2CGameSetNameColorAndRemoveBlackBar(null);
        GameManager.getInstance().sendPacketToAllClientsInSameGameSession(setNameColorAndRemoveBlackBarPacket, connection);
        GameManager.getInstance().sendPacketToAllClientsInSameGameSession(triggerGuardianServePacket, connection);
        game.resetStageStartTime();
        ThreadManager.getInstance().newTask(new DefeatTimerTask(connection, gameSession, game.getBossGuardianStage()));
    }
}

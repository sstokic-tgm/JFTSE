package com.jftse.emulator.server.core.task;

import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.matchplay.event.RunnableEvent;
import com.jftse.emulator.server.core.matchplay.event.RunnableEventHandler;
import com.jftse.emulator.server.core.matchplay.room.GameSession;
import com.jftse.emulator.server.core.thread.AbstractTask;
import com.jftse.emulator.server.database.model.battle.GuardianStage;
import com.jftse.emulator.server.networking.Connection;

import java.util.concurrent.TimeUnit;

public class DefeatTimerTask extends AbstractTask {
    private final Connection connection;
    private GameSession gameSession;
    private final GuardianStage guardianStage;

    private final RunnableEventHandler runnableEventHandler;

    public DefeatTimerTask(Connection connection, GameSession gameSession, GuardianStage guardianStage) {
        this.connection = connection;
        this.gameSession = gameSession;
        this.guardianStage = guardianStage;

        runnableEventHandler = GameManager.getInstance().getRunnableEventHandler();
    }

    @Override
    public void run() {
        if (guardianStage.getDefeatTimerInSeconds() > -1) {
            RunnableEvent runnableEvent = runnableEventHandler.createRunnableEvent(new FinishGameTask(connection, false), TimeUnit.SECONDS.toMillis(guardianStage.getDefeatTimerInSeconds()));

            gameSession.getRunnableEvents().add(runnableEvent);
            gameSession.setCountDownRunnable(runnableEvent);
        }
    }
}

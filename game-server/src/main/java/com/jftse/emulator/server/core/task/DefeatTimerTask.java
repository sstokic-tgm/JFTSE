package com.jftse.emulator.server.core.task;

import com.jftse.emulator.server.core.life.room.GameSession;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.matchplay.event.EventHandler;
import com.jftse.emulator.server.core.matchplay.event.RunnableEvent;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.battle.GuardianStage;
import com.jftse.server.core.thread.AbstractTask;

import java.util.concurrent.TimeUnit;

public class DefeatTimerTask extends AbstractTask {
    private final FTConnection connection;
    private final GameSession gameSession;
    private final GuardianStage guardianStage;

    private final EventHandler eventHandler;

    public DefeatTimerTask(FTConnection connection, GameSession gameSession, GuardianStage guardianStage) {
        this.connection = connection;
        this.gameSession = gameSession;
        this.guardianStage = guardianStage;

        eventHandler = GameManager.getInstance().getEventHandler();
    }

    @Override
    public void run() {
        if (guardianStage.getDefeatTimerInSeconds() > -1) {
            RunnableEvent runnableEvent = eventHandler.createRunnableEvent(new FinishGameTask(connection), TimeUnit.SECONDS.toMillis(guardianStage.getDefeatTimerInSeconds()));

            eventHandler.push(runnableEvent);
            gameSession.setCountDownRunnable(runnableEvent);
        }
    }
}

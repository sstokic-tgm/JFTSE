package com.jftse.emulator.server.core.task;

import com.jftse.emulator.server.core.life.room.GameSession;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.matchplay.event.EventHandler;
import com.jftse.emulator.server.core.matchplay.event.RunnableEvent;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.map.SMaps;
import com.jftse.entities.database.model.scenario.MScenarios;
import com.jftse.server.core.thread.AbstractTask;

import java.util.concurrent.TimeUnit;

public class DefeatTimerTask extends AbstractTask {
    private final FTConnection connection;
    private final GameSession gameSession;

    private final EventHandler eventHandler;

    public DefeatTimerTask(FTConnection connection, GameSession gameSession) {
        this.connection = connection;
        this.gameSession = gameSession;

        eventHandler = GameManager.getInstance().getEventHandler();
    }

    @Override
    public void run() {
        final MatchplayGuardianGame game = (MatchplayGuardianGame) gameSession.getMatchplayGame();
        final SMaps map = game.getMap();
        final MScenarios scenario = game.getScenario();

        int playTime = -1;
        if (scenario.getGameMode() == MScenarios.GameMode.GUARDIAN && map.getPlayTime() != null) {
            playTime = map.getPlayTime();
        }
        if (scenario.getGameMode() == MScenarios.GameMode.BOSS_BATTLE && map.getBossPlayTime() != null) {
            playTime = map.getBossPlayTime();
        }

        if (playTime > -1) {
            RunnableEvent runnableEvent = eventHandler.createRunnableEvent(new FinishGameTask(connection), TimeUnit.MINUTES.toMillis(playTime));

            gameSession.getFireables().push(runnableEvent);
            eventHandler.push(runnableEvent);
            gameSession.setCountDownRunnable(runnableEvent);
        }
    }
}

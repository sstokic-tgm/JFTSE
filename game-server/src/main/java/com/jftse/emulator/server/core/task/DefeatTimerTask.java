package com.jftse.emulator.server.core.task;

import com.jftse.emulator.server.core.life.room.GameSession;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.matchplay.event.EventHandler;
import com.jftse.emulator.server.core.matchplay.event.RunnableEvent;
import com.jftse.emulator.server.core.matchplay.extension.MatchTimerExtension;
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

        // Extension points get first say (e.g. a plugin mode that never times out and wants to
        // suppress the timer entirely). Falls through to the map-based logic below if none of
        // them claim this match.
        for (MatchTimerExtension ext : ServiceManager.getInstance().getMatchTimerExtensions()) {
            if (ext.overridesTimer(game)) {
                ext.applyTimer(connection, gameSession, game);
                return;
            }
        }

        int playTime = -1;
        if (scenario.getGameMode() == MScenarios.GameMode.GUARDIAN && map.getPlayTime() != null) {
            playTime = map.getPlayTime();
        }
        if ((scenario.getGameMode() == MScenarios.GameMode.BOSS_BATTLE || scenario.getGameMode() == MScenarios.GameMode.BOSS_BATTLE_V2) && map.getBossPlayTime() != null) {
            playTime = map.getBossPlayTime();
        }

        if (playTime > -1) {
            scheduleFinish(TimeUnit.MINUTES.toMillis(playTime));
        }
    }

    private void scheduleFinish(long delayMs) {
        RunnableEvent runnableEvent = eventHandler.createRunnableEvent(new FinishGameTask(connection), delayMs);

        gameSession.getFireables().push(runnableEvent);
        eventHandler.offer(runnableEvent);
        gameSession.setCountDownRunnable(runnableEvent);
    }
}

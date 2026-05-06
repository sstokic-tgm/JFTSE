package com.jftse.emulator.server.core.task;

import com.jftse.emulator.server.core.life.room.GameSession;
import com.jftse.emulator.server.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.thread.AbstractTask;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class FinishGameTask extends AbstractTask {
    private final FTConnection connection;

    public FinishGameTask(FTConnection connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        if (connection.getClient() == null) return;

        GameSession gameSession = connection.getClient().getActiveGameSession();
        if (gameSession == null) return;

        MatchplayGame game = gameSession.getMatchplayGame();

        if (game != null && !game.getFinished().get()) {
            game.getScheduledFutures().forEach(sf -> sf.cancel(false));
            game.getScheduledFutures().clear();

            game.getHandleable().onEnd(connection.getClient());
        }
    }
}

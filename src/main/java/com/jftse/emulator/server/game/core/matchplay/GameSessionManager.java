package com.jftse.emulator.server.game.core.matchplay;

import com.jftse.emulator.server.game.core.matchplay.room.GameSession;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Getter
@Log4j2
public class GameSessionManager {
    private static GameSessionManager instance;

    private CopyOnWriteArrayList<GameSession> gameSessionList;

    @PostConstruct
    public void init() {
        instance = this;
        gameSessionList = new CopyOnWriteArrayList<>();

        log.info(this.getClass().getSimpleName() + " initialized");
    }

    public static GameSessionManager getInstance() {
        return instance;
    }

    public void addGameSession(GameSession gameSession) {
        gameSessionList.add(gameSession);
    }

    public void removeGameSession(GameSession gameSession) {
        gameSessionList.remove(gameSession);
    }

    public GameSession getGameSessionBySessionId(int sessionId) {
        return gameSessionList.stream()
                .filter(gs -> gs != null && gs.getSessionId() == sessionId)
                .findFirst()
                .orElse(null);
    }
}
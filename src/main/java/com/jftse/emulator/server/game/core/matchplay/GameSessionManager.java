package com.jftse.emulator.server.game.core.matchplay;

import com.jftse.emulator.server.game.core.matchplay.room.GameSession;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Service
@Scope("singleton")
@Getter
@Setter
public class GameSessionManager {

    private List<GameSession> gameSessionList;

    @PostConstruct
    public void init() {
        gameSessionList = new ArrayList<>();
    }

    public void addGameSession(GameSession gameSession) {
        gameSessionList.add(gameSession);
    }

    public void removeGameSession(GameSession gameSession) {
        gameSessionList.remove(gameSession);
    }

    public GameSession getGameSessionBySessionId(int sessionId) {
        return gameSessionList.stream()
                .filter(gs -> gs.getSessionId() == sessionId)
                .findFirst()
                .orElse(null);
    }
}
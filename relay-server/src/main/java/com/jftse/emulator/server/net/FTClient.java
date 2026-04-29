package com.jftse.emulator.server.net;

import com.jftse.emulator.server.core.AnimationDebugStats;
import com.jftse.server.core.net.Client;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

@Getter
@Setter
public class FTClient extends Client<FTConnection> {
    private Optional<Integer> gameSessionId = Optional.empty();
    private int playerId;
    private boolean spectator;

    private final AnimationDebugStats animationDebugStats = new AnimationDebugStats();

    public void setGameSessionId(Integer gameSessionId) {
        this.gameSessionId = Optional.of(gameSessionId);
    }

    public void logAnimationDebugSummary() {
        animationDebugStats.logSummary(playerId);
    }
}

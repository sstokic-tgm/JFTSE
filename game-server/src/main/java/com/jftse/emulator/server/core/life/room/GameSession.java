package com.jftse.emulator.server.core.life.room;

import com.jftse.emulator.server.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.core.matchplay.event.RunnableEvent;
import com.jftse.emulator.server.net.FTClient;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ConcurrentLinkedDeque;

@Getter
@Setter
public class GameSession {
    public GameSession() {
        clients = new ConcurrentLinkedDeque<>();
        runnableEvents = new ConcurrentLinkedDeque<>();
    }

    private MatchplayGame matchplayGame;
    private int players;
    private int lastBallHitByPlayer = -1;
    private long timeLastBallWasHit = -1;
    private int timesCourtChanged = 0;
    private ConcurrentLinkedDeque<FTClient> clients;
    private ConcurrentLinkedDeque<RunnableEvent> runnableEvents;
    private volatile RunnableEvent countDownRunnable;

    public FTClient getClientByPlayerId(long playerId) {
        return clients.stream()
                .filter(c -> c.getPlayer() != null && c.getPlayer().getId().equals(playerId))
                .findFirst()
                .orElse(null);
    }

    public void clearCountDownRunnable() {
        if (this.getCountDownRunnable() != null) {
            this.getRunnableEvents().remove(this.getCountDownRunnable());
            this.setCountDownRunnable(null);
        }
    }
}
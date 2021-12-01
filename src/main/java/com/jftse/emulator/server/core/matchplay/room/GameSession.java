package com.jftse.emulator.server.core.matchplay.room;

import com.jftse.emulator.server.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.core.matchplay.event.RunnableEvent;
import com.jftse.emulator.server.shared.module.Client;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Getter
@Setter
public class GameSession {
    public GameSession() {
        clients = new ConcurrentLinkedDeque<>();
        clientsInRelay = new ConcurrentLinkedDeque<>();
        runnableEvents = new ConcurrentLinkedDeque<>();
    }

    private int sessionId;
    private MatchplayGame activeMatchplayGame;
    private byte players;

    private AtomicInteger lastBallHitByPlayer = new AtomicInteger(-1);
    private AtomicLong timeLastBallWasHit = new AtomicLong(-1);
    private AtomicInteger timesCourtChanged = new AtomicInteger(0);
    private ConcurrentLinkedDeque<Client> clients;
    private ConcurrentLinkedDeque<Client> clientsInRelay;
    private ConcurrentLinkedDeque<RunnableEvent> runnableEvents;
    private volatile RunnableEvent countDownRunnable;

    public Client getClientByPlayerId(long playerId) {
        return clients.stream()
                .filter(c -> c.getActivePlayer().getId().equals(playerId))
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
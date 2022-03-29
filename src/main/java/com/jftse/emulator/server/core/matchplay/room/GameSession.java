package com.jftse.emulator.server.core.matchplay.room;

import com.jftse.emulator.server.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.core.matchplay.event.RunnableEvent;
import com.jftse.emulator.server.shared.module.Client;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedDeque;

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

    private int lastBallHitByPlayer = -1;
    private long timeLastBallWasHit = -1;
    private int timesCourtChanged = 0;
    private ConcurrentLinkedDeque<Client> clients;
    private ConcurrentLinkedDeque<Client> clientsInRelay;
    private ConcurrentLinkedDeque<RunnableEvent> runnableEvents;
    private volatile RunnableEvent countDownRunnable;

    public Client getClientByPlayerId(long playerId) {
        return clients.stream()
                .filter(c -> c.getActivePlayer() != null && c.getActivePlayer().getId().equals(playerId))
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
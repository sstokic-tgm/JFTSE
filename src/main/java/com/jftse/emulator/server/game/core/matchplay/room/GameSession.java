package com.jftse.emulator.server.game.core.matchplay.room;

import com.jftse.emulator.server.game.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.game.core.matchplay.event.RunnableEvent;
import com.jftse.emulator.server.shared.module.Client;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

@Getter
@Setter
public class GameSession {
    public GameSession() {
        clients = new ConcurrentLinkedDeque<>();
        clientsInRelay = new ConcurrentLinkedDeque<>();
        runnableEvents = new ConcurrentLinkedDeque<>();
        firstSpeedHackRecognitionIgnoredForClients = new ConcurrentLinkedDeque<>();
    }

    private int sessionId;
    private MatchplayGame activeMatchplayGame;
    private byte players;

    private int lastBallHitByPlayer = -1;
    private long timeLastBallWasHit = -1;
    private int timesCourtChanged = 0;
    private ConcurrentLinkedDeque<Client> clients; // holds everything from game server
    private ConcurrentLinkedDeque<Client> clientsInRelay;
    private ConcurrentLinkedDeque<RunnableEvent> runnableEvents;
    private RunnableEvent countDownRunnable;
    private boolean speedHackCheckActive;
    private ConcurrentLinkedDeque<Client> firstSpeedHackRecognitionIgnoredForClients;

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

    public void stopSpeedHackDetection() {
        this.setSpeedHackCheckActive(false);
        this.getFirstSpeedHackRecognitionIgnoredForClients().clear();
    }
}
package com.ft.emulator.server.game.core.matchplay.room;

import com.ft.emulator.server.game.core.matchplay.MatchplayGame;
import com.ft.emulator.server.shared.module.Client;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class GameSession {
    public GameSession() {
        clients = new ArrayList<>();
    }

    private int sessionId;
    private MatchplayGame activeMatchplayGame;

    private float lastRedTeamPlayerStartX = 20;
    private float lastBlueTeamPlayerStartX = -20;
    private float redTeamPlayerStartY = -120;
    private float blueTeamPlayerStartY = 120;
    private int lastBallHitByTeam = -1;
    private long timeLastBallWasHit = -1;
    private List<Client> clients; // holds everything from game server

    public Client getClientByPlayerId(long playerId) {
        return clients.stream()
                .filter(c -> c.getActivePlayer().getId() == playerId)
                .findFirst()
                .orElse(null);
    }
}
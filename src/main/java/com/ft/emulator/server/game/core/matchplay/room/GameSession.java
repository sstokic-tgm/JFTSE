package com.ft.emulator.server.game.core.matchplay.room;

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
    private float lastRedTeamPlayerStartX = 20;
    private float lastBlueTeamPlayerStartX = -20;
    private int lastBallHitByTeam = -1;
    private long timeLastBallWasHit = -1;
    private List<Client> clients;
    private Room room;
}
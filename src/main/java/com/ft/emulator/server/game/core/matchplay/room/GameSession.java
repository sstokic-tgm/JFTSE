package com.ft.emulator.server.game.core.matchplay.room;

import com.ft.emulator.server.database.model.player.Player;
import com.ft.emulator.server.shared.module.Client;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class GameSession {
    public GameSession() {
        sessionPlayers = new ArrayList<>();
        clients = new ArrayList<>();
    }

    private int sessionId;
    private List<Player> sessionPlayers;
    private List<Client> clients;
}
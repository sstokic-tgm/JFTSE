package com.ft.emulator.server.shared.module;

import com.ft.emulator.server.database.model.account.Account;
import com.ft.emulator.server.database.model.player.Player;
import com.ft.emulator.server.game.core.matchplay.room.Room;
import com.ft.emulator.server.game.core.singleplay.challenge.ChallengeGame;
import com.ft.emulator.server.game.core.singleplay.tutorial.TutorialGame;
import com.ft.emulator.server.networking.Connection;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Client {

    private Connection connection;

    private Account account;
    private Player activePlayer;

    private ChallengeGame activeChallengeGame;
    private TutorialGame activeTutorialGame;

    private boolean inLobby;

    private Room activeRoom;
    private int lobbyGameModeTabFilter;
    private byte lobbyCurrentPlayerListPage = 1;
}

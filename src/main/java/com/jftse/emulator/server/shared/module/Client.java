package com.jftse.emulator.server.shared.module;

import com.jftse.emulator.server.database.model.account.Account;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.game.core.matchplay.room.GameSession;
import com.jftse.emulator.server.game.core.matchplay.room.Room;
import com.jftse.emulator.server.game.core.singleplay.challenge.ChallengeGame;
import com.jftse.emulator.server.game.core.singleplay.tutorial.TutorialGame;
import com.jftse.emulator.server.networking.Connection;
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
    private GameSession activeGameSession;
    private int lobbyGameModeTabFilter;
    private byte lobbyCurrentPlayerListPage = 1;
    private short lobbyCurrentRoomListPage = -1;
    private long lastHearBeatTime;

    private String ip;
    private int port;
}
package com.jftse.emulator.server.shared.module;

import com.jftse.emulator.server.core.constants.GameMode;
import com.jftse.emulator.server.database.model.account.Account;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.core.matchplay.room.GameSession;
import com.jftse.emulator.server.core.matchplay.room.Room;
import com.jftse.emulator.server.core.singleplay.challenge.ChallengeGame;
import com.jftse.emulator.server.core.singleplay.tutorial.TutorialGame;
import com.jftse.emulator.server.networking.Connection;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Getter
@Setter
public class Client {

    private Connection connection;

    private Account account;
    private Player activePlayer;

    private ChallengeGame activeChallengeGame;
    private TutorialGame activeTutorialGame;
    private Room activeRoom;
    private GameSession activeGameSession;

    private AtomicBoolean inLobby = new AtomicBoolean(false);
    private AtomicBoolean isSpectator = new AtomicBoolean(false);

    private AtomicInteger lobbyGameModeTabFilter = new AtomicInteger(GameMode.ALL);
    private AtomicInteger lobbyCurrentPlayerListPage = new AtomicInteger(1);
    private AtomicInteger lobbyCurrentRoomListPage = new AtomicInteger(-1);
    private AtomicLong lastHearBeatTime = new AtomicLong(0);

    private AtomicBoolean usingGachaMachine = new AtomicBoolean(false);

    private String ip;
    private int port;
}
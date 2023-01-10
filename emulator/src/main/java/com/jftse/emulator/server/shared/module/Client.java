package com.jftse.emulator.server.shared.module;

import com.jftse.emulator.server.core.constants.GameMode;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.matchplay.room.RoomPlayer;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.player.Player;
import com.jftse.emulator.server.core.matchplay.room.GameSession;
import com.jftse.emulator.server.core.matchplay.room.Room;
import com.jftse.emulator.server.core.singleplay.challenge.ChallengeGame;
import com.jftse.emulator.server.core.singleplay.tutorial.TutorialGame;
import com.jftse.emulator.server.networking.Connection;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Client {

    private Connection connection;

    private Long accountId;
    private Long activePlayerId;

    private ChallengeGame activeChallengeGame;
    private TutorialGame activeTutorialGame;
    private Room activeRoom;
    private RoomPlayer roomPlayer;
    private GameSession activeGameSession;

    private volatile boolean inLobby = false;
    private volatile boolean isSpectator = false;

    private volatile int lobbyGameModeTabFilter = GameMode.ALL;
    private volatile int lobbyCurrentPlayerListPage = 1;
    private volatile int lobbyCurrentRoomListPage = -1;

    private volatile boolean usingGachaMachine = false;

    // hack
    private volatile boolean requestedShopDataPrepare = false;

    private String ip;
    private int port;

    public void setPlayer(Long id) {
        this.activePlayerId = id;
    }

    public void setAccount(Long id) {
        this.accountId = id;
    }

    public Player getPlayer() {
        if (this.activePlayerId == null)
            return null;
        return ServiceManager.getInstance().getPlayerService().findById(activePlayerId);
    }

    public void savePlayer(final Player player) {
        ServiceManager.getInstance().getPlayerService().save(player);
    }

    public Account getAccount() {
        if (this.accountId == null && this.activePlayerId != null) {
            final Player player = getPlayer();
            return ServiceManager.getInstance().getAuthenticationService().findAccountById(player.getAccount().getId());
        }
        if (this.accountId == null)
            return null;
        return ServiceManager.getInstance().getAuthenticationService().findAccountById(this.accountId);
    }

    public void saveAccount(final Account account) {
        ServiceManager.getInstance().getAuthenticationService().updateAccount(account);
    }

    public RoomPlayer getRoomPlayer() {
        if (this.activeRoom == null)
            return null;

        final Room activeRoom = this.activeRoom;
        return activeRoom.getRoomPlayerList().stream()
                .filter(p -> p.getPlayerId().equals(this.activePlayerId))
                .findFirst()
                .orElse(null);
    }
}
package com.jftse.emulator.server.net;

import com.jftse.emulator.server.core.life.room.GameSession;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.matchplay.GameSessionManager;
import com.jftse.emulator.server.core.singleplay.challenge.ChallengeGame;
import com.jftse.emulator.server.core.singleplay.tutorial.TutorialGame;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.constants.GameMode;
import com.jftse.server.core.net.Client;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FTClient extends Client<FTConnection> {
    private Long accountId;
    private Long activePlayerId;

    private ChallengeGame activeChallengeGame;
    private TutorialGame activeTutorialGame;

    private Room activeRoom;
    private RoomPlayer roomPlayer;
    private Integer gameSessionId;

    private volatile boolean inLobby = false;
    private volatile boolean isSpectator = false;

    private volatile int lobbyGameModeTabFilter = GameMode.ALL;
    private volatile int lobbyCurrentPlayerListPage = 1;
    private volatile int lobbyCurrentRoomListPage = -1;

    private volatile boolean usingGachaMachine = false;

    // hack
    private volatile boolean requestedShopDataPrepare = false;

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

    public void savePlayer(Player player) {
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

    public void saveAccount(Account account) {
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

    public GameSession getActiveGameSession() {
        if (this.gameSessionId == null)
            return null;
        return GameSessionManager.getInstance().getGameSessionBySessionId(this.gameSessionId);
    }

    public void setActiveGameSession(Integer gameSessionId) {
        this.gameSessionId = gameSessionId;
    }
}

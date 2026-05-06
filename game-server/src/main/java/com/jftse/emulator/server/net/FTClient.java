package com.jftse.emulator.server.net;

import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.life.housing.FruitManager;
import com.jftse.emulator.server.core.life.room.GameSession;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.matchplay.GameSessionManager;
import com.jftse.emulator.server.core.singleplay.challenge.ChallengeGame;
import com.jftse.emulator.server.core.singleplay.tutorial.TutorialGame;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.pet.Pet;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.constants.GameMode;
import com.jftse.server.core.net.Client;
import com.jftse.server.core.shared.PlayerLoadType;
import com.jftse.server.core.shared.packets.game.SMSGReceiveData;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Getter
@Setter
public class FTClient extends Client<FTConnection> {
    private Long accountId;
    private boolean gameMaster = false;
    private Integer accountStatus;
    private final AtomicInteger ap = new AtomicInteger(0);
    private AtomicReference<FTPlayer> ftPlayer = new AtomicReference<>();

    private ChallengeGame activeChallengeGame;
    private TutorialGame activeTutorialGame;

    private Room activeRoom;
    private RoomPlayer roomPlayer;
    private Integer gameSessionId;

    private FruitManager fruitManager = new FruitManager();

    private volatile boolean inLobby = false;
    private volatile boolean isSpectator = false;

    private volatile int lobbyGameModeTabFilter = GameMode.ALL;
    private volatile int lobbyCurrentPlayerListPage = 1;
    private volatile int lobbyCurrentRoomListPage = -1;
    private volatile int highestLoadedGuildLeaguePage = 0;

    private volatile boolean usingGachaMachine = false;

    // hack
    private volatile boolean requestedShopDataPrepare = false;

    private AtomicBoolean isJoiningOrLeavingLobby = new AtomicBoolean(false);
    private AtomicBoolean isJoiningOrLeavingRoom = new AtomicBoolean(false);
    private AtomicBoolean isGoingReady = new AtomicBoolean(false);
    private AtomicBoolean isClosingSlot = new AtomicBoolean(false);
    private AtomicBoolean isChangingSlot = new AtomicBoolean(false);

    private AtomicInteger dataRequestStep = new AtomicInteger(-1);

    private int sceneId = -1;

    private Pet activePet;

    private int textMode = 0;

    public boolean updateDataRequestStep(int step) {
        boolean valid = dataRequestStep.compareAndSet(step - 1, step);

        if (!hasPlayer())
            valid = false;

        SMSGReceiveData response = SMSGReceiveData.builder()
                .dataType((byte) step)
                .unk0(valid ? (byte) 0 : (byte) 1)
                .build();
        connection.sendTCP(response);

        if (valid && step == 4) {
            connection.timeSync();
        }

        /*
        if (valid && dataRequestStep.get() == 4) {
            GameManager.getInstance().handleChatLobbyJoin(this);
        }
        */
        return valid;
    }

    public void loadPlayer(Account account, Player player, PlayerLoadType playerLoadType) {
        this.accountId = account.getId();
        this.gameMaster = account.getGameMaster();
        this.accountStatus = account.getStatus();
        this.ap.set(account.getAp());
        this.ftPlayer.set(loadPlayer(player, playerLoadType));
    }

    public FTPlayer loadPlayer(Player player, PlayerLoadType playerLoadType) {
        return switch (playerLoadType) {
            case FULL_EQUIPMENT -> FTPlayer.initWithFullEquipment(player);
            case EQUIPPED_ITEM_PARTS -> FTPlayer.initWithEquippedItemParts(player);
            case EQUIPPED_QUICK_SLOTS -> FTPlayer.initWithEquippedQuickSlots(player);
            case EQUIPPED_TOOL_SLOTS -> FTPlayer.initWithEquippedToolSlots(player);
            case EQUIPPED_SPECIAL_SLOTS -> FTPlayer.initWithEquippedSpecialSlots(player);
            case EQUIPPED_CARD_SLOTS -> FTPlayer.initWithEquippedCardSlots(player);
            default -> FTPlayer.init(player);
        };
    }

    public boolean refreshPlayer(FTPlayer player) {
        return this.ftPlayer.compareAndSet(this.ftPlayer.get(), player);
    }

    public FTPlayer getPlayer() {
        return this.ftPlayer.get();
    }

    public Optional<FTPlayer> getPlayerOptional() {
        return Optional.ofNullable(this.ftPlayer.get());
    }

    public boolean hasPlayer() {
        return this.ftPlayer.get() != null;
    }

    public Account getAccount() {
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
                .filter(p -> p.getPlayerId() == this.getPlayer().getId())
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

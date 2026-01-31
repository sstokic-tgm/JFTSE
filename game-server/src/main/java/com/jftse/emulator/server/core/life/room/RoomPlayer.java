package com.jftse.emulator.server.core.life.room;

import com.jftse.emulator.server.core.client.*;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.entities.database.model.pet.Pet;
import com.jftse.entities.database.model.player.EquippedItemStats;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.matchplay.battle.SkillCrystal;
import lombok.Getter;
import lombok.Setter;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class RoomPlayer {
    @Getter(lombok.AccessLevel.NONE)
    private final FTPlayer player;

    private boolean gameMaster;

    private AtomicInteger position = new AtomicInteger(0);
    private AtomicBoolean master = new AtomicBoolean(false);
    private AtomicBoolean ready = new AtomicBoolean(false);
    private AtomicBoolean fitting = new AtomicBoolean(false);
    private AtomicBoolean gameAnimationSkipReady = new AtomicBoolean(false);
    private AtomicBoolean connectedToRelay = new AtomicBoolean(false);
    private Long petId;

    private Queue<SkillCrystal> pickedUpSkillCrystals = new LinkedBlockingQueue<>(2);

    private long ppIdRingExp = 0;
    private long ppIdRingGold = 0;
    private long ppIdRingWiseman = 0;

    private volatile float lastX;
    private volatile float lastY;
    private volatile int lastMapLayer;

    private AtomicBoolean usedRod = new AtomicBoolean(false);
    private float baitX;
    private float baitY;

    public RoomPlayer(FTPlayer player) {
        this.player = player;
    }

    public boolean isRingOfExpEquipped() {
        final PlayerPocket pp = ServiceManager.getInstance().getPlayerPocketService().getItemAsPocketByItemIndexAndCategoryAndPocket(1, EItemCategory.SPECIAL.getName(), player.getPocketId());
        if (pp == null)
            return false;

        EquippedSpecialSlots specialSlots = player.getSpecialSlots();
        this.ppIdRingExp = specialSlots.hasItem(Math.toIntExact(pp.getId()));
        return this.ppIdRingExp != 0;
    }

    public boolean isRingOfGoldEquipped() {
        final PlayerPocket pp = ServiceManager.getInstance().getPlayerPocketService().getItemAsPocketByItemIndexAndCategoryAndPocket(2, EItemCategory.SPECIAL.getName(), player.getPocketId());
        if (pp == null)
            return false;

        EquippedSpecialSlots specialSlots = player.getSpecialSlots();
        this.ppIdRingGold = specialSlots.hasItem(Math.toIntExact(pp.getId()));
        return this.ppIdRingGold != 0;
    }

    public boolean isRingOfWisemanEquipped() {
        final PlayerPocket pp = ServiceManager.getInstance().getPlayerPocketService().getItemAsPocketByItemIndexAndCategoryAndPocket(3, EItemCategory.SPECIAL.getName(), player.getPocketId());
        if (pp == null)
            return false;

        EquippedSpecialSlots specialSlots = player.getSpecialSlots();
        this.ppIdRingWiseman = specialSlots.hasItem(Math.toIntExact(pp.getId()));
        return this.ppIdRingWiseman != 0;
    }

    public boolean isMaster() {
        return master.get();
    }

    public boolean isReady() {
        return ready.get();
    }

    public boolean isFitting() {
        return fitting.get();
    }

    public boolean isGameAnimationSkipReady() {
        return gameAnimationSkipReady.get();
    }

    public void setMaster(boolean master) {
        this.master.set(master);
    }

    public void setReady(boolean ready) {
        this.ready.set(ready);
    }

    public void setFitting(boolean fitting) {
        this.fitting.set(fitting);
    }

    public void setGameAnimationSkipReady(boolean gameAnimationSkipReady) {
        this.gameAnimationSkipReady.set(gameAnimationSkipReady);
    }

    public short getPosition() {
        return (short) position.get();
    }

    public void setPosition(short position) {
        this.position.set(position);
    }

    public Pet getPet() {
        if (petId == null)
            return null;

        return ServiceManager.getInstance().getPetService().findById(petId);
    }

    public long getAccountId() {
        return player.getAccountId();
    }

    public long getPlayerId() {
        return player.getId();
    }

    public String getName() {
        return player.getName();
    }

    public int getLevel() {
        return player.getLevel();
    }

    public int getPlayerType() {
        return player.getPlayerType();
    }

    public long getPocketId() {
        return player.getPocketId();
    }

    public long getPlayerStatisticId() {
        return player.getPlayerStatisticId();
    }

    public int getStrength() {
        return player.getStrength();
    }

    public int getStamina() {
        return player.getStamina();
    }

    public int getDexterity() {
        return player.getDexterity();
    }

    public int getWillpower() {
        return player.getWillpower();
    }

    public Long getGuildMemberId() {
        return player.getGuildMemberId();
    }

    public GuildView getGuild() {
        return player.getGuild();
    }

    public PlayerStatisticView getPlayerStatistic() {
        return player.getPlayerStatistic();
    }

    public EquippedItemParts getEquippedItemParts() {
        return player.getItemPartsPPId();
    }

    public EquippedItemParts getEquippedItemPartsIDX() {
        return player.getItemPartsItemIndex();
    }

    public EquippedItemStats getEquippedItemStats() {
        return player.getItemStats();
    }

    public EquippedSpecialSlots getEquippedSpecialSlots() {
        return player.getSpecialSlots();
    }

    public EquippedCardSlots getEquippedCardSlots() {
        return player.getCardSlots();
    }

    public EquippedQuickSlots getEquippedQuickSlots() {
        return player.getQuickSlots();
    }

    public EquippedToolSlots getEquippedToolSlots() {
        return player.getToolSlots();
    }

    public Long getCoupleId() {
        return player.getCoupleId();
    }

    public String getCoupleName() {
        return player.getCoupleName();
    }
}

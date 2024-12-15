package com.jftse.emulator.server.core.life.room;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.messenger.Friend;
import com.jftse.entities.database.model.pet.Pet;
import com.jftse.entities.database.model.player.ClothEquipment;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.player.PlayerStatistic;
import com.jftse.entities.database.model.player.StatusPointsAddedDto;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.server.core.item.EItemCategory;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class RoomPlayer {
    private Long playerId;
    private Long guildMemberId;
    private Long coupleId;
    private Long clothEquipmentId;
    private Long specialSlotEquipmentId;
    private Long cardSlotEquipmentId;
    private StatusPointsAddedDto statusPointsAddedDto;
    private AtomicInteger position = new AtomicInteger(0);
    private AtomicBoolean master = new AtomicBoolean(false);
    private AtomicBoolean ready = new AtomicBoolean(false);
    private AtomicBoolean fitting = new AtomicBoolean(false);
    private AtomicBoolean gameAnimationSkipReady = new AtomicBoolean(false);
    private AtomicBoolean connectedToRelay = new AtomicBoolean(false);
    private Long petId;

    private volatile float lastX;
    private volatile float lastY;
    private volatile int lastMapLayer;

    public Player getPlayer() {
        return ServiceManager.getInstance().getPlayerService().findById(playerId);
    }

    public GuildMember getGuildMember() {
        if (guildMemberId == null)
            return null;
        return ServiceManager.getInstance().getGuildMemberService().findById(guildMemberId);
    }

    public Friend getCouple() {
        if (coupleId == null)
            return null;
        return ServiceManager.getInstance().getFriendService().findById(coupleId);
    }

    public ClothEquipment getClothEquipment() {
        return ServiceManager.getInstance().getClothEquipmentService().findClothEquipmentById(clothEquipmentId);
    }

    public PlayerStatistic getPlayerStatistic() {
        return ServiceManager.getInstance().getPlayerStatisticService().findPlayerStatisticById(getPlayer().getPlayerStatistic().getId());
    }

    public List<Integer> getSpecialSlotEquipment() {
        return ServiceManager.getInstance().getSpecialSlotEquipmentService().getEquippedSpecialSlots(getPlayer());
    }

    public List<Integer> getCardSlotEquipment() {
        return ServiceManager.getInstance().getCardSlotEquipmentService().getEquippedCardSlots(getPlayer());
    }

    public boolean isRingOfExpEquipped() {
        final Player player = getPlayer();
        if (player == null)
            return false;

        final PlayerPocket pp = ServiceManager.getInstance().getPlayerPocketService().getItemAsPocketByItemIndexAndCategoryAndPocket(1, EItemCategory.SPECIAL.getName(), player.getPocket());
        if (pp == null)
            return false;

        final List<Integer> equippedSpecialItems = ServiceManager.getInstance().getSpecialSlotEquipmentService().getEquippedSpecialSlots(player);

        return equippedSpecialItems.contains(pp.getId().intValue());
    }

    public boolean isRingOfGoldEquipped() {
        final Player player = getPlayer();
        if (player == null)
            return false;

        final PlayerPocket pp = ServiceManager.getInstance().getPlayerPocketService().getItemAsPocketByItemIndexAndCategoryAndPocket(2, EItemCategory.SPECIAL.getName(), player.getPocket());
        if (pp == null)
            return false;

        final List<Integer> equippedSpecialItems = ServiceManager.getInstance().getSpecialSlotEquipmentService().getEquippedSpecialSlots(player);

        return equippedSpecialItems.contains(pp.getId().intValue());
    }

    public boolean isRingOfWisemanEquipped() {
        final Player player = getPlayer();
        if (player == null)
            return false;

        final PlayerPocket pp = ServiceManager.getInstance().getPlayerPocketService().getItemAsPocketByItemIndexAndCategoryAndPocket(3, EItemCategory.SPECIAL.getName(), player.getPocket());
        if (pp == null)
            return false;

        final List<Integer> equippedSpecialItems = ServiceManager.getInstance().getSpecialSlotEquipmentService().getEquippedSpecialSlots(player);

        return equippedSpecialItems.contains(pp.getId().intValue());
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
}

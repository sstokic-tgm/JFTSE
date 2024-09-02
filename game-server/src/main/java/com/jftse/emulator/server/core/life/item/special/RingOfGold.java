package com.jftse.emulator.server.core.life.item.special;

import com.jftse.emulator.server.core.life.item.BaseItem;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryItemCountPacket;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryItemsPlacePacket;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryWearSpecialAnswerPacket;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.service.*;
import com.jftse.server.core.shared.packets.inventory.S2CInventoryItemRemoveAnswerPacket;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

@Log4j2
public class RingOfGold extends BaseItem {
    private final PocketService pocketService;
    private final PlayerPocketService playerPocketService;
    private final PlayerService playerService;
    private final SpecialSlotEquipmentService specialSlotEquipmentService;

    public RingOfGold(int itemIndex, String name, String category) {
        super(itemIndex, name, category);

        pocketService = ServiceManager.getInstance().getPocketService();
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        playerService = ServiceManager.getInstance().getPlayerService();
        specialSlotEquipmentService = ServiceManager.getInstance().getSpecialSlotEquipmentService();
    }

    @Override
    public boolean processPlayer(Player player) {
        player = playerService.findById(player.getId());
        if (player == null)
            return false;

        this.localPlayerId = player.getId();

        log.info("Ring of Gold, now trying to process player, PlayerId is: " + localPlayerId);
        return true;
    }

    @Override
    public boolean processPocket(Pocket pocket) {
        pocket = pocketService.findById(pocket.getId());
        if (pocket == null)
            return false;

        List<Integer> playersSpecialSlotsToSet = new ArrayList<>();

        Player player = playerService.findById(localPlayerId);
        List<Integer> playersSpecialSlots = specialSlotEquipmentService.getEquippedSpecialSlots(player);
        int i = 1;
        for (Integer idSpecialSlot : playersSpecialSlots) {
            log.info("processing players special slots " + i + " ... id on that slot is: " + idSpecialSlot);
            i++;
        }

        PlayerPocket playerPocketROGold = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(this.getItemIndex(), this.getCategory(), pocket);
        if (playerPocketROGold == null) {
            log.info("no Gold Ring found in players pocket");
            return false;
        }

        int idOfGoldRingInPlayersPocket = playerPocketROGold.getId().intValue();
        log.info("Ring of Gold in players pocket has id: " + idOfGoldRingInPlayersPocket + " and item index: " + playerPocketROGold.getItemIndex());
        boolean playerSpecialSlotHasGoldRingEquipped = playersSpecialSlots.contains(idOfGoldRingInPlayersPocket);

        if (!playerSpecialSlotHasGoldRingEquipped){
            log.info("Ring of Gold is not equipped in players slot");
            return false;
        }

        log.info("Ring of Gold, itemCount before: " + playerPocketROGold.getItemCount());
        int itemCount = playerPocketROGold.getItemCount() - 1;
        if (itemCount <= 0) {
            playerPocketService.remove(playerPocketROGold.getId());
            pocketService.decrementPocketBelongings(pocket);

            for (Integer playersSpecialSlot : playersSpecialSlots) {
                if (playersSpecialSlot == playerPocketROGold.getId().intValue()) {
                    log.info("Special Slot value 0 added");
                    playersSpecialSlotsToSet.add(0);
                } else {
                    log.info("Special Slot value: " + playersSpecialSlot + " added");
                    playersSpecialSlotsToSet.add(playersSpecialSlot);
                }
            }
            specialSlotEquipmentService.updateSpecialSlots(player, playersSpecialSlotsToSet);
            S2CInventoryWearSpecialAnswerPacket inventoryWearSpecialAnswerPacket = new S2CInventoryWearSpecialAnswerPacket(playersSpecialSlotsToSet);
            packetsToSend.add(localPlayerId, inventoryWearSpecialAnswerPacket);

            S2CInventoryItemRemoveAnswerPacket inventoryItemRemoveAnswerPacket = new S2CInventoryItemRemoveAnswerPacket(Math.toIntExact(playerPocketROGold.getId()));
            packetsToSend.add(localPlayerId, inventoryItemRemoveAnswerPacket);
        } else {
            playerPocketROGold.setItemCount(itemCount);
            playerPocketService.save(playerPocketROGold);

            S2CInventoryItemCountPacket inventoryItemCountPacket = new S2CInventoryItemCountPacket(playerPocketROGold);
            packetsToSend.add(localPlayerId, inventoryItemCountPacket);

            specialSlotEquipmentService.updateSpecialSlots(player, playersSpecialSlots);
            S2CInventoryWearSpecialAnswerPacket inventoryWearSpecialAnswerPacket = new S2CInventoryWearSpecialAnswerPacket(playersSpecialSlots);
            packetsToSend.add(localPlayerId, inventoryWearSpecialAnswerPacket);
        }
        log.info("Ring of Gold, itemCount now: " + itemCount);
        return true;
    }
}

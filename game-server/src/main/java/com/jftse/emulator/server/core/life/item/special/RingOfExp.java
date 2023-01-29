package com.jftse.emulator.server.core.life.item.special;

import com.jftse.emulator.server.core.life.item.BaseItem;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryWearSpecialAnswerPacket;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.item.ItemSpecial;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.service.*;
import com.jftse.server.core.shared.packets.inventory.S2CInventoryItemRemoveAnswerPacket;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

@Log4j2
public class RingOfExp extends BaseItem {
    private final PocketService pocketService;
    private final PlayerPocketService playerPocketService;
    private final PlayerService playerService;
    private final SpecialSlotEquipmentService specialSlotEquipmentService;
    private final ItemSpecialService itemSpecialService;

    public RingOfExp(int itemIndex, String name, String category) {
        super(itemIndex, name, category);

        pocketService = ServiceManager.getInstance().getPocketService();
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        playerService = ServiceManager.getInstance().getPlayerService();
        specialSlotEquipmentService = ServiceManager.getInstance().getSpecialSlotEquipmentService();
        itemSpecialService = ServiceManager.getInstance().getItemSpecialService();
    }

    @Override
    public boolean processPlayer(Player player) {
        player = playerService.findById(player.getId());
        if (player == null)
            return false;

        this.localPlayerId = player.getId();

        log.info("Ring of EXP, now trying to process player, PlayerId is: " + localPlayerId);
        return true;
    }

    @Override
    public boolean processPocket(Pocket pocket) {
        pocket = pocketService.findById(pocket.getId());
        if (pocket == null)
            return false;

        Player player = playerService.findById(localPlayerId);
        List<Integer> playersSpecialSlots = specialSlotEquipmentService.getEquippedSpecialSlots(player);
        int i = 1;
        for (Integer idSpecialSlot : playersSpecialSlots) {
            log.info("processing players special slots " + i + " ... id on that slot is: " + idSpecialSlot);
            i++;
        }

        // Item-Index for EXP Ring is 1
        ItemSpecial itemSpecial = itemSpecialService.findByItemIndex(1);

        log.info("found special item: " + itemSpecial.getName() + " with item-index: " + itemSpecial.getItemIndex() + " and item-id: " + itemSpecial.getId());
        PlayerPocket playerPocketROExp = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(itemSpecial.getItemIndex(), EItemCategory.SPECIAL.getName(), pocket);
        if (playerPocketROExp == null) {
            log.info("no EXP Ring found in players pocket");
            return false;
        }

        boolean playerSpecialSlotHasExpRingEquipped = false;
        long idOfEXPRingInPlayersPocket = playerPocketROExp.getId();
        log.info("Ring of EXP in players pocket has id: " + idOfEXPRingInPlayersPocket + " and item index: " + playerPocketROExp.getItemIndex());
        for (Integer idSpecialSlot : playersSpecialSlots) {
            if (idSpecialSlot == idOfEXPRingInPlayersPocket) {
                playerSpecialSlotHasExpRingEquipped = true;
                break;
            }
        }

        if (!playerSpecialSlotHasExpRingEquipped)
            return false;

        log.info("Ring of EXP, itemCount before: " + playerPocketROExp.getItemCount());
        int itemCount = playerPocketROExp.getItemCount() - 1;
        if (itemCount <= 0) {
            final FTConnection connectionByPlayerId = GameManager.getInstance().getConnectionByPlayerId(localPlayerId);
            playerPocketService.remove(playerPocketROExp.getId());
            pocketService.decrementPocketBelongings(pocket);

            List<Integer> playersSpecialSlotsToSet = new ArrayList<>();
            List<Integer> playersSpecialSlotsToRemove = specialSlotEquipmentService.getEquippedSpecialSlots(playerService.findById(localPlayerId));

            for (Integer playersSpecialSlot : playersSpecialSlotsToRemove) {
                if (playersSpecialSlot == (Integer)playerPocketROExp.getId().intValue()){
                    log.info("Special Slot value 0 added");
                    playersSpecialSlotsToSet.add(0);
                } else {
                    log.info("Special Slot value: " + playersSpecialSlot + " added");
                    playersSpecialSlotsToSet.add(playersSpecialSlot);
                }
            }
            specialSlotEquipmentService.updateSpecialSlots(player, playersSpecialSlotsToSet);
            S2CInventoryWearSpecialAnswerPacket inventoryWearSpecialAnswerPacket = new S2CInventoryWearSpecialAnswerPacket(playersSpecialSlotsToSet);
            connectionByPlayerId.sendTCP(inventoryWearSpecialAnswerPacket);

            S2CInventoryItemRemoveAnswerPacket inventoryItemRemoveAnswerPacket = new S2CInventoryItemRemoveAnswerPacket(Math.toIntExact(playerPocketROExp.getId()));
            packetsToSend.add(localPlayerId, inventoryItemRemoveAnswerPacket);

            packetsToSend.forEach((localPlayerId, packets) -> {
                if (connectionByPlayerId != null)
                    connectionByPlayerId.sendTCP(packets.toArray(Packet[]::new));
            });
        } else {
            playerPocketROExp.setItemCount(itemCount);
            playerPocketService.save(playerPocketROExp);
        }
        log.info("Ring of EXP, itemCount now: " + itemCount);
        return true;
    }
}

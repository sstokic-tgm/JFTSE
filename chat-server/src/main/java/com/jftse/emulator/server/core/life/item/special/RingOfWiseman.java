package com.jftse.emulator.server.core.life.item.special;

import com.jftse.emulator.server.core.life.item.BaseItem;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryDataPacket;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryWearSpecialAnswerPacket;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.service.PlayerService;
import com.jftse.server.core.service.PocketService;
import com.jftse.server.core.service.SpecialSlotEquipmentService;
import com.jftse.server.core.shared.packets.inventory.S2CInventoryItemRemoveAnswerPacket;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

@Log4j2
public class RingOfWiseman extends BaseItem {
    private final PocketService pocketService;
    private final PlayerPocketService playerPocketService;
    private final PlayerService playerService;
    private final SpecialSlotEquipmentService specialSlotEquipmentService;

    public RingOfWiseman(int itemIndex, String name, String category) {
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

        log.info("Ring of Wiseman, now trying to process player, PlayerId is: " + localPlayerId);
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

        PlayerPocket playerPocketROWiseman = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(this.getItemIndex(), this.getCategory(), pocket);
        if (playerPocketROWiseman == null) {
            log.info("no Ring of Wiseman found in players pocket");
            return false;
        }

        int idOfWisemanRingInPlayersPocket = playerPocketROWiseman.getId().intValue();
        log.info("Ring of Wiseman in players pocket has id: " + idOfWisemanRingInPlayersPocket + " and item index: " + playerPocketROWiseman.getItemIndex());
        boolean playerSpecialSlotHasWisemanRingEquipped = playersSpecialSlots.contains(idOfWisemanRingInPlayersPocket);

        if (!playerSpecialSlotHasWisemanRingEquipped){
            log.info("Ring of Wiseman is not equipped in players slot");
            return false;
        }

        log.info("Ring of Wiseman, itemCount before: " + playerPocketROWiseman.getItemCount());
        int itemCount = playerPocketROWiseman.getItemCount() - 1;
        if (itemCount <= 0) {
            playerPocketService.remove(playerPocketROWiseman.getId());
            pocketService.decrementPocketBelongings(pocket);

            for (Integer playersSpecialSlot : playersSpecialSlots) {
                if (playersSpecialSlot == playerPocketROWiseman.getId().intValue()) {
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

            S2CInventoryItemRemoveAnswerPacket inventoryItemRemoveAnswerPacket = new S2CInventoryItemRemoveAnswerPacket(Math.toIntExact(playerPocketROWiseman.getId()));
            packetsToSend.add(localPlayerId, inventoryItemRemoveAnswerPacket);
        } else {
            playerPocketROWiseman.setItemCount(itemCount);
            playerPocketROWiseman.setPocket(pocket);
            playerPocketService.save(playerPocketROWiseman);
            player.setPocket(pocket);

            List<PlayerPocket> playerPocketList = new ArrayList<>();
            playerPocketList.add(playerPocketROWiseman);

            S2CInventoryDataPacket inventoryDataPacket = new S2CInventoryDataPacket(playerPocketList);
            packetsToSend.add(localPlayerId, inventoryDataPacket);

            specialSlotEquipmentService.updateSpecialSlots(player, playersSpecialSlots);
            S2CInventoryWearSpecialAnswerPacket inventoryWearSpecialAnswerPacket = new S2CInventoryWearSpecialAnswerPacket(playersSpecialSlots);
            packetsToSend.add(localPlayerId, inventoryWearSpecialAnswerPacket);
        }
        log.info("Ring of Wiseman, itemCount now: " + itemCount);
        return true;
    }
}

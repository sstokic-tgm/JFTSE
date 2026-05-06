package com.jftse.emulator.server.core.life.item.special;

import com.jftse.emulator.server.core.client.EquippedSpecialSlots;
import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.life.item.BaseItem;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryItemCountPacket;
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

    private FTPlayer player;

    public RingOfWiseman(int itemIndex, String name, String category) {
        super(itemIndex, name, category);

        pocketService = ServiceManager.getInstance().getPocketService();
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        playerService = ServiceManager.getInstance().getPlayerService();
        specialSlotEquipmentService = ServiceManager.getInstance().getSpecialSlotEquipmentService();
    }

    @Override
    public boolean processPlayer(FTPlayer player) {
        this.localPlayerId = player.getId();
        this.player = player;

        return true;
    }

    @Override
    public boolean processPocket(Long pocketId) {
        Pocket pocket = pocketService.findById(pocketId);
        if (pocket == null)
            return false;

        EquippedSpecialSlots equippedSpecialSlots = this.player.getSpecialSlots();
        if (equippedSpecialSlots == null) {
            Player player = playerService.findWithEquipmentById(this.localPlayerId);
            equippedSpecialSlots = EquippedSpecialSlots.of(player);
        }

        PlayerPocket playerPocketROWiseman = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(this.getItemIndex(), this.getCategory(), pocket);
        if (playerPocketROWiseman == null) {
            log.info("no Ring of Wiseman found in players pocket");
            return false;
        }

        int idOfWisemanRingInPlayersPocket = playerPocketROWiseman.getId().intValue();
        int wisemanRingIdInSpecialSlot = equippedSpecialSlots.hasItem(idOfWisemanRingInPlayersPocket);

        if (wisemanRingIdInSpecialSlot == 0) {
            return false;
        }

        int itemCount = playerPocketROWiseman.getItemCount() - 1;
        if (itemCount <= 0) {
            playerPocketService.remove(playerPocketROWiseman.getId());
            pocketService.decrementPocketBelongings(pocket);

            int slotIndex = equippedSpecialSlots.getSlotIndex(idOfWisemanRingInPlayersPocket);
            List<Integer> specialSlotsList = new ArrayList<>(equippedSpecialSlots.toList());
            if (slotIndex > 0) {
                specialSlotsList.set(slotIndex - 1, 0);
            }

            Player player = this.player.getPlayer();
            specialSlotEquipmentService.updateSpecialSlots(player, specialSlotsList);
            this.player.setSpecialSlots(EquippedSpecialSlots.of(player.getSpecialSlotEquipment().getId(), specialSlotsList));

            S2CInventoryWearSpecialAnswerPacket inventoryWearSpecialAnswerPacket = new S2CInventoryWearSpecialAnswerPacket(specialSlotsList);
            packetsToSend.add(localPlayerId, inventoryWearSpecialAnswerPacket);

            S2CInventoryItemRemoveAnswerPacket inventoryItemRemoveAnswerPacket = new S2CInventoryItemRemoveAnswerPacket(Math.toIntExact(playerPocketROWiseman.getId()));
            packetsToSend.add(localPlayerId, inventoryItemRemoveAnswerPacket);
        } else {
            playerPocketROWiseman.setItemCount(itemCount);
            playerPocketService.save(playerPocketROWiseman);

            S2CInventoryItemCountPacket inventoryItemCountPacket = new S2CInventoryItemCountPacket(playerPocketROWiseman);
            packetsToSend.add(localPlayerId, inventoryItemCountPacket);
        }

        return true;
    }
}

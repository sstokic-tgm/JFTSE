package com.jftse.emulator.server.core.life.item.quick;

import com.jftse.emulator.server.core.client.EquippedQuickSlots;
import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.life.item.BaseItem;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryItemCountPacket;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.service.PlayerService;
import com.jftse.server.core.service.PocketService;
import com.jftse.server.core.service.QuickSlotEquipmentService;
import com.jftse.server.core.shared.packets.inventory.S2CInventoryItemRemoveAnswerPacket;

import java.util.ArrayList;
import java.util.List;

public class QuickItem extends BaseItem {
    private final PocketService pocketService;
    private final PlayerPocketService playerPocketService;
    private final PlayerService playerService;
    private final QuickSlotEquipmentService quickSlotEquipmentService;

    private FTPlayer player;

    public QuickItem(int itemIndex) {
        super(itemIndex, "QuickItem", EItemCategory.QUICK.getName());

        pocketService = ServiceManager.getInstance().getPocketService();
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        playerService = ServiceManager.getInstance().getPlayerService();
        quickSlotEquipmentService = ServiceManager.getInstance().getQuickSlotEquipmentService();
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

        EquippedQuickSlots equippedQuickSlots = this.player.getQuickSlots();
        if (equippedQuickSlots == null) {
            Player player = playerService.findWithEquipmentById(this.localPlayerId);
            equippedQuickSlots = EquippedQuickSlots.of(player);
        }

        PlayerPocket playerPocket = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(this.getItemIndex(), this.getCategory(), pocket);
        if (playerPocket == null)
            return false;

        int idOfQuickItemInPlayerPocket = Math.toIntExact(playerPocket.getId());
        int quickItemIdInQuickItemSlot = equippedQuickSlots.hasItem(idOfQuickItemInPlayerPocket);

        if (quickItemIdInQuickItemSlot == 0) {
            return false;
        }

        int itemCount = playerPocket.getItemCount() - 1;
        if (itemCount <= 0) {
            playerPocketService.remove(playerPocket.getId());
            pocketService.decrementPocketBelongings(pocket);

            int slotIndex = equippedQuickSlots.getSlotIndex(idOfQuickItemInPlayerPocket);
            List<Integer> quickItemSlotsList = new ArrayList<>(equippedQuickSlots.toList());
            if (slotIndex > 0) {
                quickItemSlotsList.set(slotIndex - 1, 0);
            }

            Player player = this.player.getPlayer();
            quickSlotEquipmentService.updateQuickSlots(player, quickItemSlotsList);
            this.player.setQuickSlots(EquippedQuickSlots.of(player.getQuickSlotEquipment().getId(), quickItemSlotsList));

            S2CInventoryItemRemoveAnswerPacket inventoryItemRemoveAnswerPacket = new S2CInventoryItemRemoveAnswerPacket(Math.toIntExact(playerPocket.getId()));
            this.packetsToSend.add(this.localPlayerId, inventoryItemRemoveAnswerPacket);

        } else {
            playerPocket.setItemCount(itemCount);
            playerPocketService.save(playerPocket);

            S2CInventoryItemCountPacket inventoryItemCountPacket = new S2CInventoryItemCountPacket(playerPocket);
            this.packetsToSend.add(this.localPlayerId, inventoryItemCountPacket);
        }

        return true;
    }
}

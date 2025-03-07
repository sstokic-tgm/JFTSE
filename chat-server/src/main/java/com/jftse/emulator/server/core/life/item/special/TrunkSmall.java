package com.jftse.emulator.server.core.life.item.special;

import com.jftse.emulator.server.core.life.item.BaseItem;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryExpandAnswerPacket;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryItemCountPacket;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.service.PlayerService;
import com.jftse.server.core.service.PocketService;
import com.jftse.server.core.shared.packets.inventory.S2CInventoryItemRemoveAnswerPacket;

public class TrunkSmall extends BaseItem {
    private final PocketService pocketService;
    private final PlayerPocketService playerPocketService;
    private final PlayerService playerService;

    protected static final int INCREASE_CAPACITY = 10;

    public TrunkSmall(int itemIndex, String name, String category) {
        super(itemIndex, name, category);

        pocketService = ServiceManager.getInstance().getPocketService();
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        playerService = ServiceManager.getInstance().getPlayerService();
    }

    @Override
    public boolean processPlayer(Player player) {
        player = playerService.findById(player.getId());
        if (player == null)
            return false;

        this.localPlayerId = player.getId();

        return true;
    }

    @Override
    public boolean processPocket(Pocket pocket) {
        pocket = pocketService.findById(pocket.getId());
        if (pocket == null)
            return false;

        PlayerPocket playerPocketTrunkSmall = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(this.getItemIndex(), this.getCategory(), pocket);
        if (playerPocketTrunkSmall == null)
            return false;

        if (pocket.getMaxBelongings() == 300) {
            S2CInventoryExpandAnswerPacket inventoryExpandAnswerPacket = new S2CInventoryExpandAnswerPacket((byte) -1, pocket.getMaxBelongings().shortValue());
            this.packetsToSend.add(this.localPlayerId, inventoryExpandAnswerPacket);
            return false;
        }

        int newMaxBelongings = pocket.getMaxBelongings() + INCREASE_CAPACITY;
        if (newMaxBelongings > 300) {
            S2CInventoryExpandAnswerPacket inventoryExpandAnswerPacket = new S2CInventoryExpandAnswerPacket((byte) -1, pocket.getMaxBelongings().shortValue());
            this.packetsToSend.add(this.localPlayerId, inventoryExpandAnswerPacket);
            return false;
        }

        pocket.setMaxBelongings(newMaxBelongings);
        pocket = pocketService.save(pocket);

        S2CInventoryExpandAnswerPacket inventoryExpandAnswerPacket = new S2CInventoryExpandAnswerPacket((byte) 0, pocket.getMaxBelongings().shortValue());
        this.packetsToSend.add(this.localPlayerId, inventoryExpandAnswerPacket);

        int itemCount = playerPocketTrunkSmall.getItemCount() - 1;
        if (itemCount <= 0) {
            playerPocketService.remove(playerPocketTrunkSmall.getId());
            pocketService.decrementPocketBelongings(pocket);

            S2CInventoryItemRemoveAnswerPacket inventoryItemRemoveAnswerPacket = new S2CInventoryItemRemoveAnswerPacket(Math.toIntExact(playerPocketTrunkSmall.getId()));
            this.packetsToSend.add(this.localPlayerId, inventoryItemRemoveAnswerPacket);
        } else {
            playerPocketTrunkSmall.setItemCount(itemCount);
            playerPocketService.save(playerPocketTrunkSmall);

            S2CInventoryItemCountPacket inventoryItemCountPacket = new S2CInventoryItemCountPacket(playerPocketTrunkSmall);
            this.packetsToSend.add(this.localPlayerId, inventoryItemCountPacket);
        }

        return true;
    }
}

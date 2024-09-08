package com.jftse.emulator.server.core.life.item.special;

import com.jftse.emulator.server.core.life.item.BaseItem;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryItemCountPacket;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.service.PlayerService;
import com.jftse.server.core.service.PocketService;
import com.jftse.server.core.shared.packets.inventory.S2CInventoryItemRemoveAnswerPacket;

public class MegaphoneSpeaker extends BaseItem {
    private final PocketService pocketService;
    private final PlayerPocketService playerPocketService;
    private final PlayerService playerService;

    public MegaphoneSpeaker(int itemIndex, String name, String category) {
        super(itemIndex, name, category);

        this.pocketService = ServiceManager.getInstance().getPocketService();
        this.playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        this.playerService = ServiceManager.getInstance().getPlayerService();
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

        PlayerPocket megaphoneSpeaker = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(this.getItemIndex(), this.getCategory(), pocket);
        if (megaphoneSpeaker == null)
            return false;

        int itemCount = megaphoneSpeaker.getItemCount() - 1;
        if (itemCount <= 0) {
            playerPocketService.remove(megaphoneSpeaker.getId());
            pocketService.decrementPocketBelongings(pocket);

            S2CInventoryItemRemoveAnswerPacket inventoryItemRemoveAnswerPacket = new S2CInventoryItemRemoveAnswerPacket(Math.toIntExact(megaphoneSpeaker.getId()));
            this.packetsToSend.add(this.localPlayerId, inventoryItemRemoveAnswerPacket);
        } else {
            megaphoneSpeaker.setItemCount(itemCount);
            playerPocketService.save(megaphoneSpeaker);

            S2CInventoryItemCountPacket inventoryItemCountPacket = new S2CInventoryItemCountPacket(megaphoneSpeaker);
            this.packetsToSend.add(this.localPlayerId, inventoryItemCountPacket);
        }

        return true;
    }
}

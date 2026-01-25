package com.jftse.emulator.server.core.life.item.special;

import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.life.item.BaseItem;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryItemCountPacket;
import com.jftse.emulator.server.core.packets.player.S2CPlayerInfoPlayStatsPacket;
import com.jftse.emulator.server.core.packets.player.S2CPlayerStatusPointChangePacket;
import com.jftse.entities.database.model.item.ItemChar;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.service.*;
import com.jftse.server.core.shared.packets.inventory.S2CInventoryItemRemoveAnswerPacket;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class WingOfMemory extends BaseItem {
    private final PocketService pocketService;
    private final PlayerPocketService playerPocketService;
    private final ItemCharService itemCharService;
    private final PlayerService playerService;
    private final PlayerStatisticService playerStatisticService;

    public WingOfMemory(int itemIndex, String name, String category) {
        super(itemIndex, name, category);

        pocketService = ServiceManager.getInstance().getPocketService();
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        itemCharService = ServiceManager.getInstance().getItemCharService();
        playerService = ServiceManager.getInstance().getPlayerService();
        playerStatisticService = ServiceManager.getInstance().getPlayerStatisticService();
    }

    @Override
    public boolean processPlayer(FTPlayer player) {
        this.localPlayerId = player.getId();
        Player dbPlayer = playerService.findWithEquipmentById(player.getId());

        ItemChar itemChar = itemCharService.findByPlayerType((byte) player.getPlayerType());
        if (itemChar == null)
            return false;

        dbPlayer.setStrength(itemChar.getStrength());
        dbPlayer.setStamina(itemChar.getStamina());
        dbPlayer.setDexterity(itemChar.getDexterity());
        dbPlayer.setWillpower(itemChar.getWillpower());
        dbPlayer.setStatusPoints(player.getLevel() > 65 ? (byte) (65 + 5 - 1) : (byte) (player.getLevel() + 5 - 1));
        playerService.save(dbPlayer);

        player.loadItemParts(dbPlayer);

        S2CPlayerStatusPointChangePacket playerStatusPointChangePacket = new S2CPlayerStatusPointChangePacket(player);
        S2CPlayerInfoPlayStatsPacket playerInfoPlayStatsPacket = new S2CPlayerInfoPlayStatsPacket(playerStatisticService.findPlayerStatisticById(player.getPlayerStatisticId()));
        this.packetsToSend.add(this.localPlayerId, playerStatusPointChangePacket);
        this.packetsToSend.add(this.localPlayerId, playerInfoPlayStatsPacket);

        log.info("Player status points reseted by Wing of Memory, actual player level: " + player.getLevel() + " ,status points to set: " + (player.getLevel() + 5 - 1));
        return true;
    }

    @Override
    public boolean processPocket(Long pocketId) {
        Pocket pocket = pocketService.findById(pocketId);
        if (pocket == null)
            return false;

        PlayerPocket playerPocketWOM = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(this.getItemIndex(), this.getCategory(), pocket);
        if (playerPocketWOM == null)
            return false;

        int itemCount = playerPocketWOM.getItemCount() - 1;
        if (itemCount <= 0) {
            playerPocketService.remove(playerPocketWOM.getId());
            pocketService.decrementPocketBelongings(pocket);

            S2CInventoryItemRemoveAnswerPacket inventoryItemRemoveAnswerPacket = new S2CInventoryItemRemoveAnswerPacket(Math.toIntExact(playerPocketWOM.getId()));
            this.packetsToSend.add(this.localPlayerId, inventoryItemRemoveAnswerPacket);

        } else {
            playerPocketWOM.setItemCount(itemCount);
            playerPocketService.save(playerPocketWOM);

            S2CInventoryItemCountPacket inventoryItemCountPacket = new S2CInventoryItemCountPacket(playerPocketWOM);
            packetsToSend.add(localPlayerId, inventoryItemCountPacket);
        }

        return true;
    }
}

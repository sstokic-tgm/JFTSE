package com.jftse.emulator.server.core.life.item.special;

import com.jftse.emulator.server.core.life.item.BaseItem;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.player.S2CPlayerInfoPlayStatsPacket;
import com.jftse.emulator.server.core.packets.player.S2CPlayerStatusPointChangePacket;
import com.jftse.emulator.server.core.service.impl.ClothEquipmentServiceImpl;
import com.jftse.entities.database.model.item.ItemChar;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.player.StatusPointsAddedDto;
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
    private final ClothEquipmentServiceImpl clothEquipmentService;

    public WingOfMemory(int itemIndex, String name, String category) {
        super(itemIndex, name, category);

        pocketService = ServiceManager.getInstance().getPocketService();
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        itemCharService = ServiceManager.getInstance().getItemCharService();
        playerService = ServiceManager.getInstance().getPlayerService();
        clothEquipmentService = ServiceManager.getInstance().getClothEquipmentService();
    }

    @Override
    public boolean processPlayer(Player player) {
        player = playerService.findById(player.getId());
        if (player == null)
            return false;

        this.localPlayerId = player.getId();

        ItemChar itemChar = itemCharService.findByPlayerType(player.getPlayerType());
        if (itemChar == null)
            return false;

        player.setStrength(itemChar.getStrength());
        player.setStamina(itemChar.getStamina());
        player.setDexterity(itemChar.getDexterity());
        player.setWillpower(itemChar.getWillpower());
        player.setStatusPoints(player.getLevel() > 65 ? (byte) (65 + 5 - 1) : (byte) (player.getLevel() + 5 - 1));
        player = playerService.save(player);

        StatusPointsAddedDto statusPointsAddedDto = clothEquipmentService.getStatusPointsFromCloths(player);

        S2CPlayerStatusPointChangePacket playerStatusPointChangePacket = new S2CPlayerStatusPointChangePacket(player, statusPointsAddedDto);
        S2CPlayerInfoPlayStatsPacket playerInfoPlayStatsPacket = new S2CPlayerInfoPlayStatsPacket(player.getPlayerStatistic());
        this.packetsToSend.add(this.localPlayerId, playerStatusPointChangePacket);
        this.packetsToSend.add(this.localPlayerId, playerInfoPlayStatsPacket);

        log.info("Player status points reseted, actual player level: " + player.getLevel() + " ,status points to set: " + (player.getLevel() + 5 - 1));
        return true;
    }

    @Override
    public boolean processPocket(Pocket pocket) {
        pocket = pocketService.findById(pocket.getId());
        if (pocket == null)
            return false;

        PlayerPocket playerPocketWOM = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(this.getItemIndex(), this.getCategory(), pocket);
        if (playerPocketWOM == null)
            return false;

        log.info("Player status points reseted, count Wing of Memory before: " + playerPocketWOM.getItemCount());
        int itemCount = playerPocketWOM.getItemCount() - 1;
        if (itemCount <= 0) {
            playerPocketService.remove(playerPocketWOM.getId());
            pocketService.decrementPocketBelongings(pocket);

            S2CInventoryItemRemoveAnswerPacket inventoryItemRemoveAnswerPacket = new S2CInventoryItemRemoveAnswerPacket(Math.toIntExact(playerPocketWOM.getId()));
            this.packetsToSend.add(this.localPlayerId, inventoryItemRemoveAnswerPacket);

        } else {
            playerPocketWOM.setItemCount(itemCount);
            playerPocketService.save(playerPocketWOM);
        }

        log.info("Players stat points reseted, count Wing of Memory now left: " + itemCount);
        return true;
    }
}

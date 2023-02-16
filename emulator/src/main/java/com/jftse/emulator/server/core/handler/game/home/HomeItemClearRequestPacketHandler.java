package com.jftse.emulator.server.core.handler.game.home;

import com.jftse.emulator.common.utilities.StreamUtils;
import com.jftse.emulator.common.utilities.StringUtils;
import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.item.EItemCategory;
import com.jftse.emulator.server.core.item.EItemUseType;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.home.S2CHomeDataPacket;
import com.jftse.emulator.server.core.packet.packets.home.S2CHomeItemsLoadAnswerPacket;
import com.jftse.emulator.server.core.packet.packets.inventory.S2CInventoryDataPacket;
import com.jftse.emulator.server.core.service.HomeService;
import com.jftse.emulator.server.core.service.PlayerPocketService;
import com.jftse.emulator.server.core.service.PocketService;
import com.jftse.entities.database.model.home.AccountHome;
import com.jftse.entities.database.model.home.HomeInventory;
import com.jftse.entities.database.model.item.ItemHouseDeco;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.ArrayList;
import java.util.List;

public class HomeItemClearRequestPacketHandler extends AbstractHandler {
    private final HomeService homeService;
    private final PlayerPocketService playerPocketService;
    private final PocketService pocketService;

    public HomeItemClearRequestPacketHandler() {
        homeService = ServiceManager.getInstance().getHomeService();
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        pocketService = ServiceManager.getInstance().getPocketService();
    }

    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        AccountHome accountHome = homeService.findAccountHomeByAccountId(connection.getClient().getAccount().getId());
        List<HomeInventory> homeInventoryList = homeService.findAllByAccountHome(accountHome);
        Player player = connection.getClient().getPlayer();

        homeInventoryList.forEach(hil -> {
            PlayerPocket playerPocket = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(hil.getItemIndex(), EItemCategory.HOUSE_DECO.getName(), player.getPocket());
            ItemHouseDeco itemHouseDeco = homeService.findItemHouseDecoByItemIndex(hil.getItemIndex());

            // create a new one if null, null indicates that all items are placed
            if (playerPocket == null) {
                playerPocket = new PlayerPocket();
                playerPocket.setItemIndex(hil.getItemIndex());
                playerPocket.setPocket(player.getPocket());
                playerPocket.setItemCount(1);
                playerPocket.setCategory(EItemCategory.HOUSE_DECO.getName());
                playerPocket.setUseType(StringUtils.firstCharToUpperCase(EItemUseType.COUNT.getName().toLowerCase()));

                pocketService.incrementPocketBelongings(player.getPocket());
            } else {
                playerPocket.setItemCount(playerPocket.getItemCount() + 1);
            }

            playerPocketService.save(playerPocket);

            homeService.updateAccountHomeStatsByHomeInventory(accountHome, hil, false);
            homeService.removeItemFromHomeInventory(hil.getId());
        });

        S2CHomeItemsLoadAnswerPacket homeItemsLoadAnswerPacket = new S2CHomeItemsLoadAnswerPacket(new ArrayList<>());
        connection.sendTCP(homeItemsLoadAnswerPacket);

        S2CHomeDataPacket homeDataPacket = new S2CHomeDataPacket(accountHome);
        connection.sendTCP(homeDataPacket);

        List<PlayerPocket> playerPocketList = playerPocketService.getPlayerPocketItems(player.getPocket());
        StreamUtils.batches(playerPocketList, 10)
                .forEach(pocketList -> {
                    S2CInventoryDataPacket inventoryDataPacket = new S2CInventoryDataPacket(pocketList);
                    connection.sendTCP(inventoryDataPacket);
                });
    }
}

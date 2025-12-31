package com.jftse.emulator.server.core.handler.home;

import com.jftse.emulator.common.utilities.StringUtils;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.home.S2CHomeDataPacket;
import com.jftse.emulator.server.core.packets.home.S2CHomeItemsLoadAnswerPacket;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryItemsPlacePacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.home.AccountHome;
import com.jftse.entities.database.model.home.HomeInventory;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.item.EItemUseType;
import com.jftse.server.core.service.HomeService;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.service.PocketService;
import com.jftse.server.core.shared.packets.home.CMSGClearHomeItems;

import java.util.ArrayList;
import java.util.List;

@PacketId(CMSGClearHomeItems.PACKET_ID)
public class HomeItemClearRequestPacketHandler implements PacketHandler<FTConnection, CMSGClearHomeItems> {
    private final HomeService homeService;
    private final PlayerPocketService playerPocketService;
    private final PocketService pocketService;

    public HomeItemClearRequestPacketHandler() {
        homeService = ServiceManager.getInstance().getHomeService();
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        pocketService = ServiceManager.getInstance().getPocketService();
    }

    @Override
    public void handle(FTConnection connection, CMSGClearHomeItems packet) {
        FTClient client = connection.getClient();
        Player player = client.getPlayer();

        AccountHome accountHome = homeService.findAccountHomeByAccountId(client.getAccount().getId());
        List<HomeInventory> homeInventoryList = homeService.findAllByAccountHome(accountHome);
        List<PlayerPocket> ppList = playerPocketService.getPlayerPocketItemsByCategory(player.getPocket(), EItemCategory.HOUSE_DECO.getName());

        List<PlayerPocket> playerPocketsToPlace = new ArrayList<>();
        for (HomeInventory hil : homeInventoryList) {
            PlayerPocket playerPocket = ppList.stream().filter(p -> p.getItemIndex().equals(hil.getItemIndex())).findFirst().orElse(null);
            //ItemHouseDeco itemHouseDeco = homeService.findItemHouseDecoByItemIndex(hil.getItemIndex());

            // create a new one if null, null indicates that all items are placed
            if (playerPocket == null) {
                playerPocket = new PlayerPocket();
                playerPocket.setItemIndex(hil.getItemIndex());
                playerPocket.setPocket(player.getPocket());
                playerPocket.setItemCount(1);
                playerPocket.setCategory(EItemCategory.HOUSE_DECO.getName());
                playerPocket.setUseType(StringUtils.firstCharToUpperCase(EItemUseType.COUNT.getName().toLowerCase()));

                pocketService.incrementPocketBelongings(player.getPocket());

                ppList.add(playerPocket);
            } else {
                playerPocket.setItemCount(playerPocket.getItemCount() + 1);
            }
            playerPocketsToPlace.add(playerPocket);

            accountHome = homeService.updateAccountHomeStatsByHomeInventory(accountHome, hil, false);
            homeService.removeItemFromHomeInventory(hil.getId());
        }

        S2CHomeItemsLoadAnswerPacket homeItemsLoadAnswerPacket = new S2CHomeItemsLoadAnswerPacket(new ArrayList<>());
        connection.sendTCP(homeItemsLoadAnswerPacket);

        playerPocketsToPlace = playerPocketService.saveAll(playerPocketsToPlace);
        accountHome = homeService.save(accountHome);

        S2CHomeDataPacket homeDataPacket = new S2CHomeDataPacket(accountHome);
        connection.sendTCP(homeDataPacket);

        S2CInventoryItemsPlacePacket inventoryDataPacket = new S2CInventoryItemsPlacePacket(playerPocketsToPlace);
        connection.sendTCP(inventoryDataPacket);
    }
}

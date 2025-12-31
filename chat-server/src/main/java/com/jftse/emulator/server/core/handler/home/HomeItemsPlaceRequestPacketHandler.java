package com.jftse.emulator.server.core.handler.home;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.home.S2CHomeDataPacket;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryItemCountPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.home.AccountHome;
import com.jftse.entities.database.model.home.HomeInventory;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.service.HomeService;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.service.PocketService;
import com.jftse.server.core.shared.packets.home.CMSGPlaceHomeItems;
import com.jftse.server.core.shared.packets.home.HomeItem;
import com.jftse.server.core.shared.packets.inventory.S2CInventoryItemRemoveAnswerPacket;

import java.util.List;

@PacketId(CMSGPlaceHomeItems.PACKET_ID)
public class HomeItemsPlaceRequestPacketHandler implements PacketHandler<FTConnection, CMSGPlaceHomeItems> {
    private final HomeService homeService;
    private final PlayerPocketService playerPocketService;
    private final PocketService pocketService;

    public HomeItemsPlaceRequestPacketHandler() {
        homeService = ServiceManager.getInstance().getHomeService();
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        pocketService = ServiceManager.getInstance().getPocketService();
    }

    @Override
    public void handle(FTConnection connection, CMSGPlaceHomeItems homeItemsPlaceReqPacket) {
        FTClient client = connection.getClient();
        Player player = client.getPlayer();

        AccountHome accountHome = homeService.findAccountHomeByAccountId(client.getAccount().getId());
        List<PlayerPocket> ppList = playerPocketService.getPlayerPocketItemsByCategory(player.getPocket(), EItemCategory.HOUSE_DECO.getName());
        List<HomeItem> homeItemDataList = homeItemsPlaceReqPacket.getItems();

        for (HomeItem hidl : homeItemDataList) {
            int inventoryItemId = hidl.getInventoryItemId();

            if (inventoryItemId > 0) {
                PlayerPocket playerPocket = ppList.stream().filter(pp -> pp.getId().equals((long) inventoryItemId)).findFirst().orElse(null);
                if (playerPocket != null) {
                    int itemCount = playerPocket.getItemCount();

                    --itemCount;

                    if (itemCount == 0) {
                        playerPocketService.remove((long) inventoryItemId);
                        pocketService.decrementPocketBelongings(player.getPocket());

                        S2CInventoryItemRemoveAnswerPacket inventoryItemRemoveAnswerPacket = new S2CInventoryItemRemoveAnswerPacket(inventoryItemId);
                        connection.sendTCP(inventoryItemRemoveAnswerPacket);

                        ppList.remove(playerPocket);
                    } else {
                        playerPocket.setItemCount(itemCount);
                    }

                    HomeInventory homeInventory = new HomeInventory();
                    homeInventory.setId((long) inventoryItemId);
                    homeInventory.setAccountHome(accountHome);
                    homeInventory.setItemIndex(hidl.getItemIndex());
                    accountHome = setHomeInventoryPositioningAndUpdateHomeStats(accountHome, hidl, homeInventory, true);
                }
            } else if (inventoryItemId == -1) {
                // Not placed from player inventory but repositioned from home inventory
                int homeInventoryId = hidl.getHomeInventoryId();

                HomeInventory homeInventory = homeService.findById(homeInventoryId);
                if (homeInventory != null) {
                    accountHome = setHomeInventoryPositioningAndUpdateHomeStats(accountHome, hidl, homeInventory, false);
                }
            }
        }

        ppList = playerPocketService.saveAll(ppList);

        for (PlayerPocket pp : ppList) {
            S2CInventoryItemCountPacket inventoryItemCountPacket = new S2CInventoryItemCountPacket(pp);
            connection.sendTCP(inventoryItemCountPacket);
        }

        accountHome = homeService.save(accountHome);
        S2CHomeDataPacket homeDataPacket = new S2CHomeDataPacket(accountHome);
        connection.sendTCP(homeDataPacket);
    }

    private AccountHome setHomeInventoryPositioningAndUpdateHomeStats(AccountHome accountHome, HomeItem hidl, HomeInventory homeInventory, boolean updateStats) {
        homeInventory.setUnk0(hidl.getUnk0());
        homeInventory.setRotation(hidl.getRotation());
        homeInventory.setXPos(hidl.getX());
        homeInventory.setYPos(hidl.getY());
        homeInventory = homeService.save(homeInventory);

        if (updateStats) {
            return homeService.updateAccountHomeStatsByHomeInventory(accountHome, homeInventory, true);
        }
        return accountHome;
    }
}

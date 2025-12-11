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
        List<HomeItem> homeItemDataList = homeItemsPlaceReqPacket.getItems();
        AccountHome accountHome = homeService.findAccountHomeByAccountId(client.getAccount().getId());
        Player player = client.getPlayer();

        homeItemDataList.forEach(hidl -> {
            int inventoryItemId = hidl.getInventoryItemId();

            if (inventoryItemId > 0) {
                PlayerPocket playerPocket = playerPocketService.getItemAsPocket((long) inventoryItemId, player.getPocket());
                if (playerPocket != null) {
                    int itemCount = playerPocket.getItemCount();

                    --itemCount;

                    if (itemCount == 0) {
                        playerPocketService.remove((long) inventoryItemId);
                        pocketService.decrementPocketBelongings(player.getPocket());

                        S2CInventoryItemRemoveAnswerPacket inventoryItemRemoveAnswerPacket = new S2CInventoryItemRemoveAnswerPacket(inventoryItemId);
                        connection.sendTCP(inventoryItemRemoveAnswerPacket);
                    } else {
                        playerPocket.setItemCount(itemCount);
                        playerPocketService.save(playerPocket);

                        S2CInventoryItemCountPacket inventoryItemCountPacket = new S2CInventoryItemCountPacket(playerPocket);
                        connection.sendTCP(inventoryItemCountPacket);
                    }

                    HomeInventory homeInventory = new HomeInventory();
                    homeInventory.setId((long) inventoryItemId);
                    homeInventory.setAccountHome(accountHome);
                    homeInventory.setItemIndex(hidl.getItemIndex());
                    setHomeInventoryPositioningAndUpdateHomeStats(accountHome, hidl, homeInventory);
                }
            } else if (inventoryItemId == -1) {
                // Not placed from player inventory but repositioned from home inventory
                int homeInventoryId = hidl.getHomeInventoryId();

                HomeInventory homeInventory = homeService.findById(homeInventoryId);
                if (homeInventory != null) {
                    setHomeInventoryPositioningAndUpdateHomeStats(accountHome, hidl, homeInventory);
                }
            }
        });

        AccountHome upToDateAccountHome = homeService.findAccountHomeByAccountId(client.getAccount().getId());
        S2CHomeDataPacket homeDataPacket = new S2CHomeDataPacket(upToDateAccountHome);
        connection.sendTCP(homeDataPacket);
    }

    private void setHomeInventoryPositioningAndUpdateHomeStats(AccountHome accountHome, HomeItem hidl, HomeInventory homeInventory) {
        homeInventory.setUnk0(hidl.getUnk0());
        homeInventory.setRotation(hidl.getRotation());
        homeInventory.setXPos(hidl.getX());
        homeInventory.setYPos(hidl.getY());
        homeInventory = homeService.save(homeInventory);

        homeService.updateAccountHomeStatsByHomeInventory(accountHome, homeInventory, true);
    }
}

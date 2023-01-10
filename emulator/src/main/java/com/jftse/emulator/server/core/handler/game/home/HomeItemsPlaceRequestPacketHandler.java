package com.jftse.emulator.server.core.handler.game.home;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.home.C2SHomeItemsPlaceReqPacket;
import com.jftse.emulator.server.core.packet.packets.home.S2CHomeDataPacket;
import com.jftse.emulator.server.core.packet.packets.inventory.S2CInventoryItemRemoveAnswerPacket;
import com.jftse.emulator.server.core.service.HomeService;
import com.jftse.emulator.server.core.service.PlayerPocketService;
import com.jftse.emulator.server.core.service.PocketService;
import com.jftse.entities.database.model.home.AccountHome;
import com.jftse.entities.database.model.home.HomeInventory;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;
import java.util.Map;

public class HomeItemsPlaceRequestPacketHandler extends AbstractHandler {
    private C2SHomeItemsPlaceReqPacket homeItemsPlaceReqPacket;

    private final HomeService homeService;
    private final PlayerPocketService playerPocketService;
    private final PocketService pocketService;

    public HomeItemsPlaceRequestPacketHandler() {
        homeService = ServiceManager.getInstance().getHomeService();
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        pocketService = ServiceManager.getInstance().getPocketService();
    }

    @Override
    public boolean process(Packet packet) {
        homeItemsPlaceReqPacket = new C2SHomeItemsPlaceReqPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        List<Map<String, Object>> homeItemDataList = homeItemsPlaceReqPacket.getHomeItemDataList();
        AccountHome accountHome = homeService.findAccountHomeByAccountId(connection.getClient().getAccount().getId());
        Player player = connection.getClient().getPlayer();

        homeItemDataList.forEach(hidl -> {
            int inventoryItemId = (int) hidl.get("inventoryItemId");

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
                    }

                    HomeInventory homeInventory = new HomeInventory();
                    homeInventory.setId((long) inventoryItemId);
                    homeInventory.setAccountHome(accountHome);
                    homeInventory.setItemIndex((int) hidl.get("itemIndex"));
                    setHomeInventoryPositioningAndUpdateHomeStats(accountHome, hidl, homeInventory);
                }
            } else if (inventoryItemId == -1) {
                // Not placed from player inventory but repositioned from home inventory
                int homeInventoryId = (int) hidl.get("homeInventoryId");

                HomeInventory homeInventory = homeService.findById(homeInventoryId);
                if (homeInventory != null) {
                    setHomeInventoryPositioningAndUpdateHomeStats(accountHome, hidl, homeInventory);
                }
            }
        });

        AccountHome upToDateAccountHome = homeService.findAccountHomeByAccountId(connection.getClient().getAccount().getId());
        S2CHomeDataPacket homeDataPacket = new S2CHomeDataPacket(upToDateAccountHome);
        connection.sendTCP(homeDataPacket);
    }

    private void setHomeInventoryPositioningAndUpdateHomeStats(AccountHome accountHome, Map<String, Object> hidl, HomeInventory homeInventory) {
        homeInventory.setUnk0((byte) hidl.get("unk0"));
        homeInventory.setRotation((byte) hidl.get("rotation"));
        homeInventory.setXPos((byte) hidl.get("xPos"));
        homeInventory.setYPos((byte) hidl.get("yPos"));
        homeInventory = homeService.save(homeInventory);

        homeService.updateAccountHomeStatsByHomeInventory(accountHome, homeInventory, true);
    }
}

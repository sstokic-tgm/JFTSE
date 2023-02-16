package com.jftse.emulator.server.core.handler.home;

import com.jftse.emulator.server.core.packets.home.C2SHomeItemsPlaceReqPacket;
import com.jftse.emulator.server.core.packets.home.S2CHomeDataPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.home.AccountHome;
import com.jftse.entities.database.model.home.HomeInventory;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.HomeService;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.service.PocketService;
import com.jftse.server.core.shared.packets.inventory.S2CInventoryItemRemoveAnswerPacket;

import java.util.List;
import java.util.Map;

@PacketOperationIdentifier(PacketOperations.C2SHomeItemsPlaceReq)
public class HomeItemsPlaceRequestPacketHandler extends AbstractPacketHandler {
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
        FTClient client = (FTClient) connection.getClient();
        List<Map<String, Object>> homeItemDataList = homeItemsPlaceReqPacket.getHomeItemDataList();
        AccountHome accountHome = homeService.findAccountHomeByAccountId(client.getAccount().getId());
        Player player = client.getPlayer();

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

        AccountHome upToDateAccountHome = homeService.findAccountHomeByAccountId(client.getAccount().getId());
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

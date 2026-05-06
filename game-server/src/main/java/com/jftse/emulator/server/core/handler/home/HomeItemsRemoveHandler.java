package com.jftse.emulator.server.core.handler.home;

import com.jftse.emulator.common.utilities.StringUtils;
import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryItemsPlacePacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.home.AccountHome;
import com.jftse.entities.database.model.home.HomeInventory;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.item.EItemUseType;
import com.jftse.server.core.service.HomeService;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.service.PocketService;
import com.jftse.server.core.shared.packets.home.CMSGRemoveHomeItems;

import java.util.List;

@PacketId(CMSGRemoveHomeItems.PACKET_ID)
public class HomeItemsRemoveHandler implements PacketHandler<FTConnection, CMSGRemoveHomeItems> {
    private final HomeService homeService;
    private final PlayerPocketService playerPocketService;
    private final PocketService pocketService;

    public HomeItemsRemoveHandler() {
        homeService = ServiceManager.getInstance().getHomeService();
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        pocketService = ServiceManager.getInstance().getPocketService();
    }

    @Override
    public void handle(FTConnection connection, CMSGRemoveHomeItems packet) {
        FTClient client = connection.getClient();
        FTPlayer player = client.getPlayer();
        Pocket pocket = pocketService.findById(player.getPocketId());

        AccountHome accountHome = homeService.findAccountHomeByAccountId(client.getAccount().getId());
        HomeInventory hiItem = homeService.findById(packet.getHomeInventoryId());
        if (hiItem != null && hiItem.getAccountHome().getId().equals(accountHome.getId())) {
            PlayerPocket playerPocket = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(hiItem.getItemIndex(), EItemCategory.HOUSE_DECO.getName(), pocket);
            if (playerPocket == null) {
                playerPocket = new PlayerPocket();
                playerPocket.setItemIndex(hiItem.getItemIndex());
                playerPocket.setPocket(pocket);
                playerPocket.setItemCount(1);
                playerPocket.setCategory(EItemCategory.HOUSE_DECO.getName());
                playerPocket.setUseType(StringUtils.firstCharToUpperCase(EItemUseType.COUNT.getName().toLowerCase()));

                pocketService.incrementPocketBelongings(pocket);
            } else {
                playerPocket.setItemCount(playerPocket.getItemCount() + 1);
            }

            playerPocket = playerPocketService.save(playerPocket);

            accountHome = homeService.updateAccountHomeStatsByHomeInventory(accountHome, hiItem, false);
            homeService.save(accountHome);
            homeService.removeItemFromHomeInventory(hiItem.getId());

            S2CInventoryItemsPlacePacket inventoryDataPacket = new S2CInventoryItemsPlacePacket(List.of(playerPocket));
            connection.sendTCP(inventoryDataPacket);
        }
    }
}

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
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.item.EItemUseType;
import com.jftse.server.core.service.HomeService;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.service.PocketService;
import com.jftse.server.core.shared.packets.home.CMSGClearHomeItems;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        Pocket pocket = pocketService.findById(player.getPocket().getId());

        AccountHome accountHome = homeService.findAccountHomeByAccountId(client.getAccount().getId());
        List<HomeInventory> homeInventoryList = homeService.findAllByAccountHome(accountHome);
        if (homeInventoryList.isEmpty()) {
            S2CHomeItemsLoadAnswerPacket homeItemsLoadAnswerPacket = new S2CHomeItemsLoadAnswerPacket(new ArrayList<>());
            connection.sendTCP(homeItemsLoadAnswerPacket);
            return;
        }

        List<PlayerPocket> ppList = playerPocketService.getPlayerPocketItemsByCategory(pocket, EItemCategory.HOUSE_DECO.getName());
        Map<Integer, PlayerPocket> pocketByItemIndex = ppList.stream()
                .collect(
                        Collectors.toMap(
                                PlayerPocket::getItemIndex,
                                Function.identity(),
                                (a, b) -> a)
                );

        Map<Integer, Integer> returnedCounts = new HashMap<>();
        for (HomeInventory homeInventory : homeInventoryList) {
            returnedCounts.merge(homeInventory.getItemIndex(), 1, Integer::sum);
        }

        List<PlayerPocket> playerPocketsToPlace = new ArrayList<>(returnedCounts.size());
        for (Map.Entry<Integer, Integer> entry : returnedCounts.entrySet()) {
            Integer itemIndex = entry.getKey();
            Integer count = entry.getValue();

            PlayerPocket playerPocket = pocketByItemIndex.get(itemIndex);
            // create a new one if null, null indicates that all items are placed
            if (playerPocket == null) {
                playerPocket = new PlayerPocket();
                playerPocket.setItemIndex(itemIndex);
                playerPocket.setPocket(pocket);
                playerPocket.setItemCount(count);
                playerPocket.setCategory(EItemCategory.HOUSE_DECO.getName());
                playerPocket.setUseType(StringUtils.firstCharToUpperCase(EItemUseType.COUNT.getName().toLowerCase()));

                pocket.setBelongings(pocket.getBelongings() + 1);
                pocketByItemIndex.put(itemIndex, playerPocket);
                playerPocketsToPlace.add(playerPocket);
            } else {
                playerPocket.setItemCount(playerPocket.getItemCount() + count);
            }
        }

        homeService.removeAllHomeItemsByAccountHome(accountHome);
        accountHome = homeService.subtractStatsForRemovedHomeItems(accountHome, returnedCounts);
        accountHome = homeService.save(accountHome);

        pocketService.save(pocket);
        playerPocketsToPlace = playerPocketService.saveAll(playerPocketsToPlace);

        connection.sendTCP(new S2CHomeItemsLoadAnswerPacket(new ArrayList<>()));
        connection.sendTCP(new S2CHomeDataPacket(accountHome));
        connection.sendTCP(new S2CInventoryItemsPlacePacket(playerPocketsToPlace));
    }
}

package com.jftse.emulator.server.core.task;

import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.chat.S2CChatLobbyAnswerPacket;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryItemCountPacket;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryItemsPlacePacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.item.Product;
import com.jftse.entities.database.model.lottery.LotteryItemDto;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.service.LotteryService;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.service.PocketService;
import com.jftse.server.core.service.ProductService;
import com.jftse.server.core.shared.packets.inventory.S2CInventoryItemRemoveAnswerPacket;
import com.jftse.server.core.thread.AbstractTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GachaMachineTask extends AbstractTask {
    private final FTConnection connection;
    private final List<String> commandArgumentList;

    private final ProductService productService;
    private final PocketService pocketService;
    private final PlayerPocketService playerPocketService;
    private final LotteryService lotteryService;

    public GachaMachineTask(FTConnection connection, List<String> commandArgumentList) {
        this.connection = connection;
        this.commandArgumentList = commandArgumentList;

        this.productService = ServiceManager.getInstance().getProductService();
        this.pocketService = ServiceManager.getInstance().getPocketService();
        this.playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        this.lotteryService = ServiceManager.getInstance().getLotteryService();
    }

    @Override
    public void run() {
        FTClient client = connection.getClient();
        if (!client.hasPlayer()) {
            return;
        }

        FTPlayer player = client.getPlayer();
        Pocket pocket = pocketService.findById(player.getPocketId());

        String gachaName = commandArgumentList.get(0);
        Integer count = Integer.valueOf(commandArgumentList.get(1));
        Product product = productService.findProductByName(gachaName, EItemCategory.LOTTERY.getName());
        if (product != null) {
            if (!product.getCategory().equals(EItemCategory.LOTTERY.getName())) {
                S2CChatLobbyAnswerPacket chatLobbyAnswerPacket = new S2CChatLobbyAnswerPacket((char) 0, "GachaMachine", gachaName + " is not a gacha.");
                connection.sendTCP(chatLobbyAnswerPacket);
                return;
            }

            List<LotteryItemDto> lotteryItemList = lotteryService.getLotteryItemsByGachaIndex(player.getPlayerType(), product.getItem0());

            PlayerPocket playerPocketGacha = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(product.getItem0(), product.getCategory(), pocket);
            if (playerPocketGacha != null) {
                connection.getClient().setUsingGachaMachine(true);

                S2CChatLobbyAnswerPacket chatLobbyAnswerPacket = new S2CChatLobbyAnswerPacket((char) 0, "GachaMachine", "Please wait in lobby until it finishes.");
                connection.sendTCP(chatLobbyAnswerPacket);

                chatLobbyAnswerPacket = new S2CChatLobbyAnswerPacket((char) 0, "GachaMachine", "Opening " + product.getName() + "...");
                connection.sendTCP(chatLobbyAnswerPacket);

                List<PlayerPocket> result = new ArrayList<>();
                Map<String, Integer> resultDisplay = new HashMap<>();
                if (count > playerPocketGacha.getItemCount())
                    count = playerPocketGacha.getItemCount();
                else if (count <= 0)
                    count = 1;

                for (int i = 0; i < count; i++) {
                    if ((i % 500) == 0 && i != 0) {
                        chatLobbyAnswerPacket = new S2CChatLobbyAnswerPacket((char) 0, "GachaMachine", "Current opening progress: " + ((i * 100) / count) + "%");

                        connection.sendTCP(chatLobbyAnswerPacket);
                    }

                    playerPocketGacha.setItemCount(playerPocketGacha.getItemCount() - 1);
                    result.add(lotteryService.drawLottery(lotteryItemList, pocket));
                }

                int itemCount = playerPocketGacha.getItemCount();
                if (itemCount <= 0) {
                    pocketService.decrementPocketBelongings(player.getPocketId());
                    playerPocketService.remove(playerPocketGacha.getId());

                    // if current count is 0 remove the item
                    S2CInventoryItemRemoveAnswerPacket inventoryItemRemoveAnswerPacket = new S2CInventoryItemRemoveAnswerPacket(Math.toIntExact(playerPocketGacha.getId()));
                    connection.sendTCP(inventoryItemRemoveAnswerPacket);
                } else {
                    playerPocketGacha.setItemCount(itemCount);
                    playerPocketGacha = playerPocketService.save(playerPocketGacha);

                    S2CInventoryItemCountPacket inventoryItemCountPacket = new S2CInventoryItemCountPacket(playerPocketGacha);
                    connection.sendTCP(inventoryItemCountPacket);
                }

                chatLobbyAnswerPacket = new S2CChatLobbyAnswerPacket((char) 0, "GachaMachine", "Finished. Preparing drawn items...");
                connection.sendTCP(chatLobbyAnswerPacket);

                Map<Integer, PlayerPocket> filteredResult = new HashMap<>();
                for (PlayerPocket pp : result) {
                    PlayerPocket existing = filteredResult.get(pp.getItemIndex());

                    // we only keep the playerpocket with highest item count in result
                    if (existing == null || pp.getItemCount() > existing.getItemCount()) {
                        filteredResult.put(pp.getItemIndex(), pp);
                    }

                    Product p = this.productService.findProductByItemAndCategory(pp.getItemIndex(), pp.getCategory());
                    Integer timesDrawn = resultDisplay.get(p.getName()) == null ? 0 : resultDisplay.get(p.getName());
                    timesDrawn++;
                    resultDisplay.put(p.getName(), timesDrawn);
                }
                result = new ArrayList<>(filteredResult.values());

                if (connection.getClient() != null)
                    connection.getClient().setUsingGachaMachine(false);

                String resultText = "Drawn items: (count represents how often it was drawn)";
                chatLobbyAnswerPacket = new S2CChatLobbyAnswerPacket((char) 0, "GachaMachine", resultText);
                connection.sendTCP(chatLobbyAnswerPacket);

                resultDisplay.forEach((key, value) -> {
                    S2CChatLobbyAnswerPacket chatLobbyResultItemPacket = new S2CChatLobbyAnswerPacket((char) 0, "GachaMachine", value + "x - " + key);
                    connection.sendTCP(chatLobbyResultItemPacket);
                });

                S2CInventoryItemsPlacePacket inventoryDataPacket = new S2CInventoryItemsPlacePacket(result);
                connection.sendTCP(inventoryDataPacket);
            } else {
                S2CChatLobbyAnswerPacket chatLobbyAnswerPacket = new S2CChatLobbyAnswerPacket((char) 0, "GachaMachine", "You do not have this gacha.");
                connection.sendTCP(chatLobbyAnswerPacket);
            }
        } else {
            S2CChatLobbyAnswerPacket chatLobbyAnswerPacket = new S2CChatLobbyAnswerPacket((char) 0, "GachaMachine", "Gacha not found.");
            connection.sendTCP(chatLobbyAnswerPacket);
        }
    }
}

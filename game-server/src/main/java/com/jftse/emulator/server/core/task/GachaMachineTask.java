package com.jftse.emulator.server.core.task;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.chat.S2CChatLobbyAnswerPacket;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryItemsPlacePacket;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.item.Product;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.service.LotteryService;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.service.PocketService;
import com.jftse.server.core.service.ProductService;
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
        if (connection.getClient() == null)
            return;

        Player player = connection.getClient().getPlayer();
        Pocket pocket = pocketService.findById(player.getPocket().getId());

        String gachaName = commandArgumentList.get(0);
        Integer count = Integer.valueOf(commandArgumentList.get(1));
        Product product = productService.findProductByName(gachaName, EItemCategory.LOTTERY.getName());
        if (product != null) {
            if (!product.getCategory().equals(EItemCategory.LOTTERY.getName())) {
                S2CChatLobbyAnswerPacket chatLobbyAnswerPacket = new S2CChatLobbyAnswerPacket((char) 0, "GachaMachine", gachaName + " is not a gacha.");
                connection.sendTCP(chatLobbyAnswerPacket);
                return;
            }

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
                    result.addAll(lotteryService.drawLottery(connection, playerPocketGacha.getId(), product.getProductIndex()));
                }
                chatLobbyAnswerPacket = new S2CChatLobbyAnswerPacket((char) 0, "GachaMachine", "Finished. Preparing drawn items...");
                connection.sendTCP(chatLobbyAnswerPacket);

                result.forEach(pp -> {
                    Product p = this.productService.findProductByItemAndCategory(pp.getItemIndex(), pp.getCategory());
                    Integer itemCount = resultDisplay.get(p.getName()) == null ? 0 : resultDisplay.get(p.getName());
                    itemCount++;
                    resultDisplay.put(p.getName(), itemCount);
                });

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

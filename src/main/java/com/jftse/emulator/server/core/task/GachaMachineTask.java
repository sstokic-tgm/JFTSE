package com.jftse.emulator.server.core.task;

import com.jftse.emulator.common.utilities.StreamUtils;
import com.jftse.emulator.server.core.item.EItemCategory;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.chat.S2CChatLobbyAnswerPacket;
import com.jftse.emulator.server.core.packet.packets.inventory.S2CInventoryDataPacket;
import com.jftse.emulator.server.core.service.*;
import com.jftse.emulator.server.core.thread.AbstractTask;
import com.jftse.emulator.server.database.model.item.Product;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.database.model.pocket.PlayerPocket;
import com.jftse.emulator.server.database.model.pocket.Pocket;
import com.jftse.emulator.server.networking.Connection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GachaMachineTask extends AbstractTask {
    private final Connection connection;
    private final List<String> commandArgumentList;

    private final PlayerService playerService;
    private final ProductService productService;
    private final PocketService pocketService;
    private final PlayerPocketService playerPocketService;
    private final LotteryService lotteryService;

    public GachaMachineTask(Connection connection, List<String> commandArgumentList) {
        this.connection = connection;
        this.commandArgumentList = commandArgumentList;

        this.playerService = ServiceManager.getInstance().getPlayerService();
        this.productService = ServiceManager.getInstance().getProductService();
        this.pocketService = ServiceManager.getInstance().getPocketService();
        this.playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        this.lotteryService = ServiceManager.getInstance().getLotteryService();
    }

    @Override
    public void run() {
        Player player = playerService.findById(connection.getClient().getActivePlayer().getId());
        Pocket pocket = pocketService.findById(player.getPocket().getId());

        String gachaName = commandArgumentList.get(0);
        Integer count = Integer.valueOf(commandArgumentList.get(1));
        Product product = productService.findProductByName(gachaName, EItemCategory.LOTTERY.getName());
        if (product != null) {
            if (!product.getCategory().equals(EItemCategory.LOTTERY.getName())) {
                S2CChatLobbyAnswerPacket chatLobbyAnswerPacket = new S2CChatLobbyAnswerPacket((char) 0, "GachaMachine", gachaName + " is not a gacha.");
                if (connection.isConnected())
                    connection.sendTCP(chatLobbyAnswerPacket);
                return;
            }

            PlayerPocket playerPocketGacha = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(product.getItem0(), product.getCategory(), pocket);
            if (playerPocketGacha != null) {
                connection.getClient().setUsingGachaMachine(true);

                S2CChatLobbyAnswerPacket chatLobbyAnswerPacket = new S2CChatLobbyAnswerPacket((char) 0, "GachaMachine", "Please wait in lobby until it finishes.");
                if (connection.isConnected())
                    connection.sendTCP(chatLobbyAnswerPacket);

                chatLobbyAnswerPacket = new S2CChatLobbyAnswerPacket((char) 0, "GachaMachine", "Opening " + product.getName() + "...");
                if (connection.isConnected())
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

                        if (connection.isConnected())
                            connection.sendTCP(chatLobbyAnswerPacket);
                    }
                    result.addAll(lotteryService.drawLottery(connection, playerPocketGacha.getId(), product.getProductIndex()));
                }
                chatLobbyAnswerPacket = new S2CChatLobbyAnswerPacket((char) 0, "GachaMachine", "Finished. Preparing drawn items...");
                if (connection.isConnected())
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
                if (connection.isConnected())
                    connection.sendTCP(chatLobbyAnswerPacket);

                resultDisplay.forEach((key, value) -> {
                    S2CChatLobbyAnswerPacket chatLobbyResultItemPacket = new S2CChatLobbyAnswerPacket((char) 0, "GachaMachine", value + "x - " + key);
                    if (connection.isConnected())
                        connection.sendTCP(chatLobbyResultItemPacket);
                });

                List<PlayerPocket> playerPocketList = playerPocketService.getPlayerPocketItems(pocket);
                StreamUtils.batches(playerPocketList, 10).forEach(pocketList -> {
                    S2CInventoryDataPacket inventoryDataPacket = new S2CInventoryDataPacket(pocketList);
                    if (connection.isConnected())
                        connection.sendTCP(inventoryDataPacket);
                });
            } else {
                S2CChatLobbyAnswerPacket chatLobbyAnswerPacket = new S2CChatLobbyAnswerPacket((char) 0, "GachaMachine", "You do not have this gacha.");
                if (connection.isConnected())
                    connection.sendTCP(chatLobbyAnswerPacket);
            }
        } else {
            S2CChatLobbyAnswerPacket chatLobbyAnswerPacket = new S2CChatLobbyAnswerPacket((char) 0, "GachaMachine", "Gacha not found.");
            if (connection.isConnected())
                connection.sendTCP(chatLobbyAnswerPacket);
        }
    }
}

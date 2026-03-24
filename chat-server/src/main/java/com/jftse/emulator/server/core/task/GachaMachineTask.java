package com.jftse.emulator.server.core.task;

import com.jftse.emulator.common.exception.ValidationException;
import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.life.lottery.GachaOpenResult;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.chat.S2CChatLobbyAnswerPacket;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryItemCountPacket;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryItemsPlacePacket;
import com.jftse.emulator.server.core.service.LotteryServiceV2;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.item.Product;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.service.ProductService;
import com.jftse.server.core.shared.packets.inventory.S2CInventoryItemRemoveAnswerPacket;
import com.jftse.server.core.thread.AbstractTask;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
public class GachaMachineTask extends AbstractTask {
    private final FTConnection connection;
    private final List<String> commandArgumentList;

    private final ProductService productService;
    private final PlayerPocketService playerPocketService;
    private final LotteryServiceV2 lotteryService;

    public GachaMachineTask(FTConnection connection, List<String> commandArgumentList) {
        this.connection = connection;
        this.commandArgumentList = commandArgumentList;

        this.productService = ServiceManager.getInstance().getProductService();
        this.playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        this.lotteryService = ServiceManager.getInstance().getLotteryServiceV2();
    }

    @Override
    public void run() {
        FTClient client = connection.getClient();
        if (!client.hasPlayer()) {
            return;
        }

        FTPlayer player = client.getPlayer();

        final String gachaName = commandArgumentList.get(0);
        final int count = Integer.parseInt(commandArgumentList.get(1));

        Product gacha = productService.findProductByName(gachaName, EItemCategory.LOTTERY.getName());
        if (gacha == null) {
            S2CChatLobbyAnswerPacket chatLobbyAnswerPacket = new S2CChatLobbyAnswerPacket((char) 0, "GachaMachine", "Gacha not found.");
            connection.sendTCP(chatLobbyAnswerPacket);
            return;
        }

        PlayerPocket gachaPP = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(gacha.getItem0(), gacha.getCategory(), player.getPocketId());
        if (gachaPP == null) {
            S2CChatLobbyAnswerPacket chatLobbyAnswerPacket = new S2CChatLobbyAnswerPacket((char) 0, "GachaMachine", "You do not have this gacha.");
            connection.sendTCP(chatLobbyAnswerPacket);
            return;
        }

        try {
            client.setUsingGachaMachine(true);

            S2CChatLobbyAnswerPacket
                    chatLobbyAnswerPacket = new S2CChatLobbyAnswerPacket((char) 0, "GachaMachine", "Opening " + gacha.getName() + "...");
            connection.sendTCP(chatLobbyAnswerPacket, new S2CChatLobbyAnswerPacket((char) 0, "GachaMachine", "Please wait in lobby until it finishes."));

            List<GachaOpenResult> resultList = lotteryService.openGacha(client, gachaName, count, (i, result) -> {
                if ((i % 500) == 0 && i != 0) {
                    S2CChatLobbyAnswerPacket tmp = new S2CChatLobbyAnswerPacket((char) 0, "GachaMachine", "Current opening progress: " + ((i * 100) / count) + "%");
                    connection.sendTCP(tmp);
                }
            });

            Map<String, Integer> resultDisplay = new HashMap<>();
            Map<Integer, PlayerPocket> filteredResult = new HashMap<>();

            // only last item in list holds info if gacha was consumed
            GachaOpenResult lastResult = resultList.getLast();
            if (lastResult.isConsumedGachaRemoved()) {
                S2CInventoryItemRemoveAnswerPacket inventoryItemRemoveAnswerPacket = new S2CInventoryItemRemoveAnswerPacket(Math.toIntExact(gachaPP.getId()));
                connection.sendTCP(inventoryItemRemoveAnswerPacket);
            } else if (lastResult.getConsumedGachaPocket() != null) {
                S2CInventoryItemCountPacket inventoryItemCountPacket = new S2CInventoryItemCountPacket(lastResult.getConsumedGachaPocket());
                connection.sendTCP(inventoryItemCountPacket);
            }

            String resultText = "Finished. Preparing drawn items...";
            chatLobbyAnswerPacket = new S2CChatLobbyAnswerPacket((char) 0, "GachaMachine", resultText);
            connection.sendTCP(chatLobbyAnswerPacket);

            for (GachaOpenResult result : resultList) {
                if (result.isSuccess()) {
                    Product p = this.productService.findProductByItemAndCategory(result.getAwardedItem().getItemIndex(), result.getAwardedItem().getCategory());
                    Integer timesDrawn = resultDisplay.get(p.getName()) == null ? 0 : resultDisplay.get(p.getName());
                    timesDrawn++;
                    resultDisplay.put(p.getName(), timesDrawn);

                    PlayerPocket existing = filteredResult.get(result.getAwardedItem().getItemIndex());

                    // we only keep the playerpocket with highest item count in result
                    if (existing == null || result.getAwardedItem().getItemCount() > existing.getItemCount()) {
                        filteredResult.put(result.getAwardedItem().getItemIndex(), result.getAwardedItem());
                    }
                } else {
                    log.debug("Gacha open failed for player {}: {}", client.getPlayer().getName(), result.getFailureReason());

                    chatLobbyAnswerPacket = new S2CChatLobbyAnswerPacket((char) 0, "GachaMachine", result.getFailureReason());
                    connection.sendTCP(chatLobbyAnswerPacket);
                }
            }

            List<PlayerPocket> finalResult = new ArrayList<>(filteredResult.values());

            resultText = "Drawn items: (count represents how often it was drawn)";
            chatLobbyAnswerPacket = new S2CChatLobbyAnswerPacket((char) 0, "GachaMachine", resultText);
            connection.sendTCP(chatLobbyAnswerPacket);

            resultDisplay.forEach((key, value) -> {
                S2CChatLobbyAnswerPacket chatLobbyResultItemPacket = new S2CChatLobbyAnswerPacket((char) 0, "GachaMachine", value + "x - " + key);
                connection.sendTCP(chatLobbyResultItemPacket);
            });

            S2CInventoryItemsPlacePacket inventoryDataPacket = new S2CInventoryItemsPlacePacket(finalResult);
            connection.sendTCP(inventoryDataPacket);

        } catch (ValidationException e) {
            log.error("Failed to open gacha for player {}: {}", client.getPlayer().getName(), e.getMessage());

            S2CChatLobbyAnswerPacket chatLobbyAnswerPacket = new S2CChatLobbyAnswerPacket((char) 0, "GachaMachine", "Failed to open gacha.");
            connection.sendTCP(chatLobbyAnswerPacket);
        } finally {
            client.setUsingGachaMachine(false);
        }
    }
}

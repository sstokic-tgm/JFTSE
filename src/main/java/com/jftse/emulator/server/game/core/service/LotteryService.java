package com.jftse.emulator.server.game.core.service;

import com.jftse.emulator.common.utilities.ResourceUtil;
import com.jftse.emulator.common.utilities.StringUtils;
import com.jftse.emulator.server.database.model.item.Product;
import com.jftse.emulator.server.database.model.lottery.LotteryItemDto;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.database.model.pocket.PlayerPocket;
import com.jftse.emulator.server.database.model.pocket.Pocket;
import com.jftse.emulator.server.game.core.item.EItemCategory;
import com.jftse.emulator.server.game.core.item.EItemChar;
import com.jftse.emulator.server.game.core.packet.packets.inventory.S2CInventoryDataPacket;
import com.jftse.emulator.server.game.core.packet.packets.inventory.S2CInventoryItemRemoveAnswerPacket;
import com.jftse.emulator.server.networking.Connection;
import lombok.RequiredArgsConstructor;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class LotteryService {

    private final PlayerService playerService;
    private final ProductService productService;
    private final PlayerPocketService playerPocketService;
    private final PocketService pocketService;

    private Random random;

    @PostConstruct
    public void init() {
        random = new Random();
    }

    public List<PlayerPocket> drawLottery(Connection connection, long playerPocketId, int productIndex) {
        Player player = connection.getClient().getActivePlayer();

        List<PlayerPocket> result = new ArrayList<>();

        handlePlayerPocket(connection, playerPocketId);

        List<Product> productList = productService.findProductsByItemList(Stream.of(productIndex).collect(Collectors.toList()));
        List<LotteryItemDto> lotteryItemList = getLotteryItemsByGachaIndex(player.getPlayerType(), productList.get(0).getItem0());
        LotteryItemDto lotteryItem = pickItemLotteryFromList(lotteryItemList);

        productList.clear();
        Product winningItem = productService.findProductsByItemList(Stream.of(lotteryItem.getShopIndex()).collect(Collectors.toList())).get(0);

        Pocket pocket = pocketService.findById(player.getPocket().getId());

        PlayerPocket playerPocket = saveWinningItem(winningItem, lotteryItem, pocket);
        result.add(playerPocket);

        player.setPocket(playerPocket.getPocket());
        player = playerService.save(player);

        connection.getClient().setActivePlayer(player);

        return result;
    }

    private void handlePlayerPocket(Connection connection, long playerPocketId) {
        PlayerPocket playerPocket = playerPocketService.findById(playerPocketId);

        if (playerPocket != null) {
            int itemCount = playerPocket.getItemCount() - 1;

            if (itemCount <= 0) {
                pocketService.decrementPocketBelongings(playerPocket.getPocket());
                playerPocketService.remove(playerPocket.getId());

                // if current count is 0 remove the item
                S2CInventoryItemRemoveAnswerPacket inventoryItemRemoveAnswerPacket = new S2CInventoryItemRemoveAnswerPacket((int) playerPocketId);
                connection.sendTCP(inventoryItemRemoveAnswerPacket);
            }
            else {
                playerPocket.setItemCount(itemCount);
                playerPocket = playerPocketService.save(playerPocket);

                // to have current coin count
                S2CInventoryDataPacket inventoryDataPacket = new S2CInventoryDataPacket(Arrays.asList(playerPocket));
                connection.sendTCP(inventoryDataPacket);
            }
        }
    }

    private List<LotteryItemDto> getLotteryItemsByGachaIndex(byte playerType, int gachaIndex) {
        List<LotteryItemDto> result = new ArrayList<>();

        String playerTypeName = StringUtils.firstCharToUpperCase(EItemChar.getNameByValue(playerType).toLowerCase());

        try {
            InputStream lotteryItemFile = ResourceUtil.getResource("res/lottery/Ini3_Lot_" + (gachaIndex < 10 ? ("0" + gachaIndex) : gachaIndex ) + ".xml");
            SAXReader reader = new SAXReader();
            reader.setEncoding("UTF-8");
            Document document = reader.read(lotteryItemFile);

            List<Node> lotteryItemList = document.selectNodes("/LotteryItemList/LotteryItem_" + playerTypeName);

            for (int i = 0; i < lotteryItemList.size(); i++) {

                Node lotteryItem = lotteryItemList.get(i);

                LotteryItemDto lotteryItemDto = new LotteryItemDto();
                lotteryItemDto.setShopIndex(Integer.valueOf(lotteryItem.valueOf("@ShopIndex")));
                lotteryItemDto.setQuantityMin(Integer.valueOf(lotteryItem.valueOf("@QuantityMin")));
                lotteryItemDto.setQuantityMax(Integer.valueOf(lotteryItem.valueOf("@QuantityMax")));
                lotteryItemDto.setChansPer(Double.valueOf(lotteryItem.valueOf("@ChansPer")));

                result.add(lotteryItemDto);
            }
            lotteryItemFile.close();
        }
        catch (DocumentException | IOException de) {
            return new ArrayList<>();
        }

        return result;
    }

    private LotteryItemDto pickItemLotteryFromList(List<LotteryItemDto> lotteryItemList) {
        double sum = lotteryItemList.stream().mapToDouble(LotteryItemDto::getChansPer).sum();
        double randomNum = random.nextDouble() * sum;

        double end = 0.0;
        for (LotteryItemDto lotteryItem : lotteryItemList) {
            end += lotteryItem.getChansPer();

            if (end >= randomNum)
                return lotteryItem;
        }
        return null;
    }

    private PlayerPocket saveWinningItem(Product winningItem, LotteryItemDto lotteryItem, Pocket pocket) {
        PlayerPocket playerPocket = playerPocketService.getItemAsPocketByItemIndexAndPocket(winningItem.getItem0(), pocket);

        int existingItemCount = 0;
        boolean existingItem = false;
        boolean existingPartItem = false;

        if (playerPocket != null && !playerPocket.getCategory().equals(EItemCategory.PARTS.getName())) {
            existingItemCount = playerPocket.getItemCount();
            existingItem = true;
        }
        else if (playerPocket != null && playerPocket.getCategory().equals(EItemCategory.PARTS.getName())) {
            existingPartItem = true;
        }
        else {
            playerPocket = new PlayerPocket();
        }

        if (!existingPartItem) {
            playerPocket.setCategory(winningItem.getCategory());
            playerPocket.setItemIndex(winningItem.getItem0());
            playerPocket.setUseType(winningItem.getUseType());
            playerPocket.setItemCount(lotteryItem.getQuantityMin() + existingItemCount);
            playerPocket.setPocket(pocket);

            playerPocket = playerPocketService.save(playerPocket);
            if (!existingItem)
                pocketService.incrementPocketBelongings(playerPocket.getPocket());
        }
        return playerPocket;
    }
}

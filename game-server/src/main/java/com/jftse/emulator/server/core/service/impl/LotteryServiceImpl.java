package com.jftse.emulator.server.core.service.impl;

import com.jftse.emulator.common.utilities.ResourceUtil;
import com.jftse.emulator.common.utilities.StringUtils;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryDataPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.entities.database.model.item.Product;
import com.jftse.entities.database.model.lottery.LotteryItemDto;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.item.EItemChar;
import com.jftse.server.core.item.EItemUseType;
import com.jftse.server.core.net.Client;
import com.jftse.server.core.net.Connection;
import com.jftse.server.core.service.LotteryService;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.service.PocketService;
import com.jftse.server.core.service.ProductService;
import com.jftse.server.core.shared.packets.inventory.S2CInventoryItemRemoveAnswerPacket;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class LotteryServiceImpl implements LotteryService {
    private final ProductService productService;
    private final PlayerPocketService playerPocketService;
    private final PocketService pocketService;

    private Random random;

    @PostConstruct
    @Override
    public void init() {
        random = new Random();
    }

    @Override
    public List<PlayerPocket> drawLottery(Connection<? extends Client<?>> connection, long playerPocketId, int productIndex) {
        FTClient ftClient = (FTClient) connection.getClient();
        Player player = ftClient.getPlayer();

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
        ftClient.savePlayer(player);

        return result;
    }

    private void handlePlayerPocket(Connection<?> connection, long playerPocketId) {
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
        PlayerPocket playerPocket = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(winningItem.getItem0(), winningItem.getCategory(), pocket);

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

            if (playerPocket.getUseType().equalsIgnoreCase(EItemUseType.TIME.getName())) {
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                cal.add(Calendar.DAY_OF_MONTH, playerPocket.getItemCount());

                playerPocket.setCreated(cal.getTime());
            }

            playerPocket.setPocket(pocket);

            playerPocket = playerPocketService.save(playerPocket);
            if (!existingItem)
                pocketService.incrementPocketBelongings(playerPocket.getPocket());
        }
        return playerPocket;
    }
}

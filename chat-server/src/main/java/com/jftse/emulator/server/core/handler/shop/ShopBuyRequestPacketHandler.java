package com.jftse.emulator.server.core.handler.shop;

import com.jftse.emulator.server.core.packets.home.S2CHomeDataPacket;
import com.jftse.emulator.server.core.packets.shop.C2SShopBuyPacket;
import com.jftse.emulator.server.core.packets.shop.S2CShopBuyPacket;
import com.jftse.emulator.server.core.packets.shop.S2CShopMoneyAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.entities.database.converters.PriceTypeConverter;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.auctionhouse.PriceType;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.item.EItemUseType;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.home.AccountHome;
import com.jftse.entities.database.model.item.ItemHouse;
import com.jftse.entities.database.model.item.Product;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.*;
import org.springframework.util.ReflectionUtils;

import java.util.*;

@PacketOperationIdentifier(PacketOperations.C2SShopBuyReq)
public class ShopBuyRequestPacketHandler extends AbstractPacketHandler {
    private C2SShopBuyPacket shopBuyPacket;

    private final ProductService productService;
    private final HomeService homeService;
    private final PlayerPocketService playerPocketService;
    private final PocketService pocketService;
    private final PlayerService playerService;

    public ShopBuyRequestPacketHandler() {
        productService = ServiceManager.getInstance().getProductService();
        homeService = ServiceManager.getInstance().getHomeService();
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        pocketService = ServiceManager.getInstance().getPocketService();
        playerService = ServiceManager.getInstance().getPlayerService();
    }

    @Override
    public boolean process(Packet packet) {
        shopBuyPacket = new C2SShopBuyPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient ftClient = (FTClient) connection.getClient();

        Map<Integer, Byte> itemList = shopBuyPacket.getItemList();
        Map<Product, Byte> productList = productService.findProductsByItemList(itemList);

        Account account = ftClient.getAccount();
        Player player = ftClient.getPlayer();

        int gold = player.getGold();
        int ap = account.getAp();

        int costsGold = productList.entrySet()
                .stream()
                .filter(entry -> entry.getKey().getPriceType().equals(PriceType.GOLD.getName()))
                .mapToInt(entry -> {
                    byte option = entry.getValue();
                    if (option <= 0)
                        return entry.getKey().getPrice0();
                    else if (option == 1)
                        return entry.getKey().getPrice1();
                    else
                        return entry.getKey().getPrice2();
                })
                .sum();

        int resultGold = gold - costsGold;
        if (resultGold < 0) {
            S2CShopBuyPacket shopBuyPacketAnswer = new S2CShopBuyPacket(S2CShopBuyPacket.NEED_MORE_GOLD, null);
            connection.sendTCP(shopBuyPacketAnswer);
            return;
        }

        int costsAp = productList.entrySet()
                .stream()
                .filter(entry -> entry.getKey().getPriceType().equals(PriceType.MINT.getName()))
                .mapToInt(entry -> {
                    byte option = entry.getValue();
                    if (option <= 0)
                        return entry.getKey().getPrice0();
                    else if (option == 1)
                        return entry.getKey().getPrice1();
                    else
                        return entry.getKey().getPrice2();
                })
                .sum();

        int resultAp = ap - costsAp;
        if (resultAp < 0) {
            S2CShopBuyPacket shopBuyPacketAnswer = new S2CShopBuyPacket(S2CShopBuyPacket.NEED_MORE_CASH, null);
            connection.sendTCP(shopBuyPacketAnswer);
            return;
        }

        List<PlayerPocket> playerPocketList = new ArrayList<>();
        for (Map.Entry<Product, Byte> data : productList.entrySet()) {
            Product product = data.getKey();
            byte option = data.getValue();

            // prevent user from buying pet till it'simplemented
            if (product.getCategory().equals(EItemCategory.PET_CHAR.getName())) {
                resultGold += product.getPrice0();
                continue;
            }

            if (!product.getCategory().equals(EItemCategory.CHAR.getName())) {
                if (product.getCategory().equals(EItemCategory.HOUSE.getName())) {

                    ItemHouse itemHouse = homeService.findItemHouseByItemIndex(product.getItem0());
                    AccountHome accountHome = homeService.findAccountHomeByAccountId(ftClient.getAccount().getId());

                    accountHome.setLevel(itemHouse.getLevel());
                    accountHome = homeService.save(accountHome);

                    S2CHomeDataPacket homeDataPacket = new S2CHomeDataPacket(accountHome);
                    connection.sendTCP(homeDataPacket);
                } else {
                    // gold back
                    if (product.getGoldBack() != 0)
                        resultGold += product.getGoldBack();

                    Pocket pocket = player.getPocket();

                    if (product.getItem1() != 0) {

                        List<Integer> itemPartList = new ArrayList<>();

                        // use reflection to get indexes of item0-9
                        ReflectionUtils.doWithFields(product.getClass(), field -> {

                            if (field.getName().startsWith("item")) {

                                field.setAccessible(true);

                                Integer itemIndex = (Integer) field.get(product);
                                if (itemIndex != 0) {
                                    itemPartList.add(itemIndex);
                                }

                                field.setAccessible(false);
                            }
                        });

                        // case if set has player included, items are transferred to the new player
                        if (product.getForPlayer() != -1) {

                            Player newPlayer = productService.createNewPlayer(ftClient.getAccount(), product.getForPlayer());
                            Pocket newPlayerPocket = pocketService.findById(newPlayer.getPocket().getId());

                            for (Integer itemIndex : itemPartList) {

                                PlayerPocket playerPocket = new PlayerPocket();
                                playerPocket.setCategory(product.getCategory());
                                playerPocket.setItemIndex(itemIndex);
                                playerPocket.setUseType(product.getUseType());
                                playerPocket.setItemCount(1);
                                playerPocket.setPocket(newPlayerPocket);

                                playerPocketService.save(playerPocket);
                                newPlayerPocket = pocketService.incrementPocketBelongings(newPlayerPocket);
                            }
                        } else {
                            for (Integer itemIndex : itemPartList) {

                                PlayerPocket playerPocket = new PlayerPocket();
                                playerPocket.setCategory(product.getCategory());
                                playerPocket.setItemIndex(itemIndex);
                                playerPocket.setUseType(product.getUseType());
                                playerPocket.setItemCount(1);
                                playerPocket.setPocket(pocket);

                                playerPocket = playerPocketService.save(playerPocket);
                                pocket = pocketService.incrementPocketBelongings(pocket);

                                // add item to result
                                playerPocketList.add(playerPocket);
                            }
                        }
                    } else {
                        PlayerPocket playerPocket = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(product.getItem0(), product.getCategory(), player.getPocket());
                        int existingItemCount = 0;
                        boolean existingItem = false;

                        if (playerPocket != null && !playerPocket.getUseType().equals("N/A")) {
                            existingItemCount = playerPocket.getItemCount();
                            existingItem = true;
                        } else {
                            playerPocket = new PlayerPocket();
                        }

                        playerPocket.setCategory(product.getCategory());
                        playerPocket.setItemIndex(product.getItem0());
                        playerPocket.setUseType(product.getUseType());

                        if (option <= 0)
                            playerPocket.setItemCount(product.getUse0() == 0 ? 1 : product.getUse0());
                        else if (option == 1)
                            playerPocket.setItemCount(product.getUse1());
                        else
                            playerPocket.setItemCount(product.getUse2());

                        // no idea how itemCount can be null here, but ok
                        playerPocket.setItemCount((playerPocket.getItemCount() == null ? 0 : playerPocket.getItemCount()) + existingItemCount);

                        if (playerPocket.getUseType().equalsIgnoreCase(EItemUseType.TIME.getName())) {
                            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                            cal.add(Calendar.DAY_OF_MONTH, playerPocket.getItemCount());

                            playerPocket.setCreated(cal.getTime());
                            playerPocket.setItemCount(1);
                        }
                        playerPocket.setPocket(pocket);

                        playerPocket = playerPocketService.save(playerPocket);
                        if (!existingItem)
                            pocket = pocketService.incrementPocketBelongings(pocket);

                        // add item to result
                        playerPocketList.add(playerPocket);
                    }

                    player.setPocket(pocket);
                }
            } else {
                productService.createNewPlayer(ftClient.getAccount(), product.getForPlayer());
            }
        }

        S2CShopBuyPacket shopBuyPacketAnswer = new S2CShopBuyPacket(S2CShopBuyPacket.SUCCESS, playerPocketList);
        connection.sendTCP(shopBuyPacketAnswer);

        playerService.setMoney(player, resultGold);
        account.setAp(resultAp);
        ftClient.saveAccount(account);

        S2CShopMoneyAnswerPacket shopMoneyAnswerPacket = new S2CShopMoneyAnswerPacket(player);
        connection.sendTCP(shopMoneyAnswerPacket);
    }
}

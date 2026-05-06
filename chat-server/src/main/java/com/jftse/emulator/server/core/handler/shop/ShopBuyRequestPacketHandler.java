package com.jftse.emulator.server.core.handler.shop;

import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.life.event.GameEventBus;
import com.jftse.emulator.server.core.life.event.GameEventType;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.pet.S2CPetAddPacket;
import com.jftse.emulator.server.core.packets.shop.S2CShopBuyPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.auctionhouse.PriceType;
import com.jftse.entities.database.model.item.Product;
import com.jftse.entities.database.model.pet.Pet;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.item.GoldBackAdded;
import com.jftse.server.core.item.PetCreated;
import com.jftse.server.core.service.InventoryService;
import com.jftse.server.core.service.PlayerService;
import com.jftse.server.core.service.ProductService;
import com.jftse.server.core.shared.packets.shop.CMSGShopBuy;
import com.jftse.server.core.shared.packets.shop.SMSGSetMoney;
import com.jftse.server.core.shared.packets.shop.ShopBuy;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
@PacketId(CMSGShopBuy.PACKET_ID)
public class ShopBuyRequestPacketHandler implements PacketHandler<FTConnection, CMSGShopBuy> {
    private final ProductService productService;
    private final PlayerService playerService;
    private final InventoryService inventoryService;

    public ShopBuyRequestPacketHandler() {
        productService = ServiceManager.getInstance().getProductService();
        playerService = ServiceManager.getInstance().getPlayerService();
        inventoryService = ServiceManager.getInstance().getInventoryService();
    }

    @Override
    public void handle(FTConnection connection, CMSGShopBuy shopBuyPacket) {
        FTClient client = connection.getClient();
        if (!client.hasPlayer())
            return;

        Account account = client.getAccount();

        List<ShopBuy> itemList = shopBuyPacket.getItemList();
        Map<Product, Byte> productList = productService.findProductsByItemList2(itemList);

        if (productList.size() == 1) {
            boolean noBuy = productList.keySet()
                    .stream()
                    .allMatch(Product::getNoBuy);
            if (noBuy) {
                S2CShopBuyPacket shopBuyPacketAnswer = new S2CShopBuyPacket(S2CShopBuyPacket.NOT_FOR_SALE_LIMIT_PRODUCT, null);
                connection.sendTCP(shopBuyPacketAnswer);
                return;
            }
        } else {
            boolean success = productList.keySet().removeIf(Product::getNoBuy);
            if (success && productList.isEmpty()) {
                S2CShopBuyPacket shopBuyPacketAnswer = new S2CShopBuyPacket(S2CShopBuyPacket.NOT_FOR_SALE_LIMIT_PRODUCT, null);
                connection.sendTCP(shopBuyPacketAnswer);
                return;
            }
        }

        FTPlayer player = client.getPlayer();

        int gold = player.getGold();
        int ap = client.getAp().get();

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

        AtomicInteger resultGold = new AtomicInteger(gold - costsGold);
        if (resultGold.get() < 0) {
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

            int quantity = switch (option) {
                case 0 -> product.getUse0() == 0 ? 1 : product.getUse0();
                case 1 -> product.getUse1();
                case 2 -> product.getUse2();
                default -> 1;
            };

            List<PlayerPocket> result = inventoryService.addItem(
                    player.getId(), product.getProductIndex(), quantity,
                    List.of(
                            h -> {
                                if (h instanceof PetCreated(Pet pet)) {
                                    S2CPetAddPacket petAddPacket = new S2CPetAddPacket(pet);
                                    connection.sendTCP(petAddPacket);
                                }
                            },
                            h -> {
                                if (h instanceof GoldBackAdded(int goldBack)) {
                                    resultGold.addAndGet(goldBack);
                                }
                            }
                    )
            );
            playerPocketList.addAll(result);
        }

        S2CShopBuyPacket shopBuyPacketAnswer = new S2CShopBuyPacket(S2CShopBuyPacket.SUCCESS, playerPocketList);
        connection.sendTCP(shopBuyPacketAnswer);

        int newGold = Math.max(resultGold.get(), 0);
        player.syncGold(newGold);
        boolean success = client.getAp().compareAndSet(ap, resultAp);
        playerService.setMoney(player.getPlayer(), resultGold.get());
        if (!success) {
            log.warn("Failed to update AP for player {} ({}): expected {}, actual {}, new {}",
                    player.getName(),
                    player.getId(),
                    ap,
                    client.getAp().get(),
                    resultAp);
        }
        account.setAp(resultAp);
        client.saveAccount(account);

        SMSGSetMoney moneyPacket = SMSGSetMoney.builder()
                .ap(client.getAp().get())
                .gold(player.getGold())
                .build();
        connection.sendTCP(moneyPacket);

        GameEventBus.call(GameEventType.SHOP_ITEM_BOUGHT, client, playerPocketList, costsGold, costsAp);
    }
}

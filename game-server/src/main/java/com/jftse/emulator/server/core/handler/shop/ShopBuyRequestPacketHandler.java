package com.jftse.emulator.server.core.handler.shop;

import com.jftse.emulator.server.core.life.event.GameEventBus;
import com.jftse.emulator.server.core.life.event.GameEventType;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.pet.S2CPetAddPacket;
import com.jftse.emulator.server.core.packets.shop.S2CShopBuyPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.item.PetCreated;
import com.jftse.server.core.service.ShopTransactionService;
import com.jftse.server.core.service.transaction.*;
import com.jftse.server.core.shared.packets.shop.CMSGShopBuy;
import com.jftse.server.core.shared.packets.shop.SMSGSetMoney;

@PacketId(CMSGShopBuy.PACKET_ID)
public class ShopBuyRequestPacketHandler implements PacketHandler<FTConnection, CMSGShopBuy> {
    private final ShopTransactionService shopTransactionService;

    public ShopBuyRequestPacketHandler() {
        shopTransactionService = ServiceManager.getInstance().getShopTransactionService();
    }

    @Override
    public void handle(FTConnection connection, CMSGShopBuy packet) {
        FTClient client = connection.getClient();
        if (!client.hasPlayer()) {
            return;
        }
        ShopTransactionResult result;
        try {
            result = shopTransactionService.buy(client.getAccountId(), client.getPlayer().getId(), packet.getItemList());
        } catch (TransactionException exception) {
            short status = switch (exception.getReason()) {
                case INSUFFICIENT_GOLD -> S2CShopBuyPacket.NEED_MORE_GOLD;
                case INSUFFICIENT_AP -> S2CShopBuyPacket.NEED_MORE_CASH;
                default -> S2CShopBuyPacket.NOT_FOR_SALE_LIMIT_PRODUCT;
            };
            connection.sendTCP(new S2CShopBuyPacket(status, null));
            return;
        } catch (ArithmeticException exception) {
            connection.sendTCP(new S2CShopBuyPacket(S2CShopBuyPacket.ERROR_ITEM_LIMIT, null));
            return;
        }
        TransactionNotifications.afterCommit(() -> {
            client.getPlayer().syncGold(result.gold());
            client.getAp().set(result.ap());
            result.postActions().forEach(action -> {
                if (action instanceof PetCreated created) {
                    connection.sendTCP(new S2CPetAddPacket(created.pet()));
                }
            });
            connection.sendTCP(new S2CShopBuyPacket(S2CShopBuyPacket.SUCCESS, result.items()));
            SMSGSetMoney moneyPacket = SMSGSetMoney.builder()
                    .gold(result.gold())
                    .ap(result.ap())
                    .build();
            connection.sendTCP(moneyPacket);
            GameEventBus.call(GameEventType.SHOP_ITEM_BOUGHT, client, result.items(), result.costsGold(), result.costsAp());
        });
    }
}

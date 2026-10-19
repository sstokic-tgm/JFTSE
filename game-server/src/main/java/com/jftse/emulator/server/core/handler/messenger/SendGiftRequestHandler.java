package com.jftse.emulator.server.core.handler.messenger;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryItemsPlacePacket;
import com.jftse.emulator.server.core.packets.messenger.S2CReceivedGiftNotificationPacket;
import com.jftse.emulator.server.core.packets.messenger.S2CSendGiftAnswerPacket;
import com.jftse.emulator.server.core.packets.pet.S2CPetAddPacket;
import com.jftse.emulator.server.core.packets.shop.S2CShopBuyPacket;
import com.jftse.emulator.server.core.rabbit.service.RProducerService;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.item.AddItemHook;
import com.jftse.server.core.item.PetCreated;
import com.jftse.server.core.service.GiftTransactionService;
import com.jftse.server.core.service.transaction.GiftTransactionResult;
import com.jftse.server.core.service.transaction.TransactionException;
import com.jftse.server.core.service.transaction.TransactionNotifications;
import com.jftse.server.core.shared.packets.messenger.CMSGSendGift;
import com.jftse.server.core.shared.packets.shop.SMSGSetMoney;
import com.jftse.server.core.shared.rabbit.messages.PacketMessage;

@PacketId(CMSGSendGift.PACKET_ID)
public class SendGiftRequestHandler implements PacketHandler<FTConnection, CMSGSendGift> {
    private final GiftTransactionService giftTransactionService;
    private final RProducerService rProducerService;

    public SendGiftRequestHandler() {
        giftTransactionService = ServiceManager.getInstance().getGiftTransactionService();
        rProducerService = RProducerService.getInstance();
    }

    @Override
    public void handle(FTConnection connection, CMSGSendGift packet) {
        FTClient client = connection.getClient();
        if (client == null || !client.hasPlayer()) {
            return;
        }

        GiftTransactionResult result;
        try {
            result = giftTransactionService.send(client.getAccountId(), client.getPlayer().getId(),
                    packet.getReceiverName(), packet.getProductIndex(), packet.getOption(), packet.getMessage());
        } catch (TransactionException exception) {
            if (exception.getReason() == TransactionException.Reason.NOT_FOUND) {
                return;
            }
            short status = switch (exception.getReason()) {
                case INSUFFICIENT_GOLD -> S2CShopBuyPacket.NEED_MORE_GOLD;
                case INSUFFICIENT_AP -> S2CShopBuyPacket.NEED_MORE_CASH;
                default -> -9;
            };
            connection.sendTCP(new S2CSendGiftAnswerPacket(status, null));
            return;
        } catch (ArithmeticException exception) {
            connection.sendTCP(new S2CSendGiftAnswerPacket((short) -9, null));
            return;
        }

        TransactionNotifications.afterCommit(() -> {
            client.getPlayer().syncGold(result.gold());
            client.getAp().set(result.ap());
            for (AddItemHook action : result.postActions()) {
                if (action instanceof PetCreated created) {
                    connection.sendTCP(new S2CPetAddPacket(created.pet()));
                }
            }

            S2CReceivedGiftNotificationPacket giftPacket = new S2CReceivedGiftNotificationPacket(result.gift());
            S2CInventoryItemsPlacePacket inventoryPacket = new S2CInventoryItemsPlacePacket(result.items());
            PacketMessage giftMessage = PacketMessage.builder()
                    .receivingPlayerId(result.gift().getReceiver().getId())
                    .packet(giftPacket)
                    .build();
            PacketMessage inventoryMessage = PacketMessage.builder()
                    .receivingPlayerId(result.gift().getReceiver().getId())
                    .packet(inventoryPacket)
                    .build();
            String origin = client.getPlayer().getName() + "(GameServer)";
            rProducerService.send(giftMessage, "game.messenger.gift chat.messenger.gift", origin);
            rProducerService.send(inventoryMessage, "game.messenger.gift chat.messenger.gift", origin);

            connection.sendTCP(new S2CSendGiftAnswerPacket(S2CShopBuyPacket.SUCCESS, result.gift()));
            SMSGSetMoney moneyPacket = SMSGSetMoney.builder()
                    .ap(result.ap())
                    .gold(result.gold())
                    .build();
            connection.sendTCP(moneyPacket);
        });
    }
}

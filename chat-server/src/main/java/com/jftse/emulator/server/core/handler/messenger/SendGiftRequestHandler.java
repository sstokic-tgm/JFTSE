package com.jftse.emulator.server.core.handler.messenger;

import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryItemsPlacePacket;
import com.jftse.emulator.server.core.packets.messenger.S2CReceivedGiftNotificationPacket;
import com.jftse.emulator.server.core.packets.messenger.S2CSendGiftAnswerPacket;
import com.jftse.emulator.server.core.packets.pet.S2CPetAddPacket;
import com.jftse.emulator.server.core.packets.shop.S2CShopBuyPacket;
import com.jftse.emulator.server.core.rabbit.service.RProducerService;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.auctionhouse.PriceType;
import com.jftse.entities.database.model.item.Product;
import com.jftse.entities.database.model.messenger.Gift;
import com.jftse.entities.database.model.pet.Pet;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.item.GoldBackAdded;
import com.jftse.server.core.item.PetCreated;
import com.jftse.server.core.service.GiftService;
import com.jftse.server.core.service.InventoryService;
import com.jftse.server.core.service.PlayerService;
import com.jftse.server.core.service.ProductService;
import com.jftse.server.core.shared.packets.messenger.CMSGSendGift;
import com.jftse.server.core.shared.packets.shop.SMSGSetMoney;
import com.jftse.server.core.shared.rabbit.messages.PacketMessage;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
@PacketId(CMSGSendGift.PACKET_ID)
public class SendGiftRequestHandler implements PacketHandler<FTConnection, CMSGSendGift> {
    private final PlayerService playerService;
    private final ProductService productService;
    private final GiftService giftService;
    private final InventoryService inventoryService;

    private final RProducerService rProducerService;

    public SendGiftRequestHandler() {
        playerService = ServiceManager.getInstance().getPlayerService();
        productService = ServiceManager.getInstance().getProductService();
        giftService = ServiceManager.getInstance().getGiftService();
        inventoryService = ServiceManager.getInstance().getInventoryService();
        rProducerService = RProducerService.getInstance();
    }

    @Override
    public void handle(FTConnection connection, CMSGSendGift packet) {
        FTClient ftClient = connection.getClient();
        if (!ftClient.hasPlayer())
            return;

        byte option = packet.getOption();

        Product product = productService.findProductByProductItemIndex(packet.getProductIndex());
        FTPlayer sender = ftClient.getPlayer();
        Account account = ftClient.getAccount();
        Player receiver = playerService.findByName(packet.getReceiverName());

        if (receiver != null && product != null) {
            if (!product.getEnabled()) {
                S2CSendGiftAnswerPacket s2CSendGiftAnswerPacket = new S2CSendGiftAnswerPacket((short) -9, null);
                connection.sendTCP(s2CSendGiftAnswerPacket);
                return;
            }
            if (sender.getLevel() < 20) {
                S2CSendGiftAnswerPacket s2CSendGiftAnswerPacket = new S2CSendGiftAnswerPacket((short) -9, null);
                connection.sendTCP(s2CSendGiftAnswerPacket);
                return;
            }

            List<Gift> gifts = giftService.findWithPlayerByReceiver(receiver.getId());
            List<Gift> senderGifts = giftService.findWithPlayerBySender(sender.getId());
            if (gifts.size() > 128 || senderGifts.size() > 128) {
                S2CSendGiftAnswerPacket s2CSendGiftAnswerPacket = new S2CSendGiftAnswerPacket((short) -9, null);
                connection.sendTCP(s2CSendGiftAnswerPacket);
                return;
            }

            Gift gift = new Gift();
            gift.setReceiver(receiver);
            gift.setSender(sender.getPlayer());
            gift.setMessage(packet.getMessage());
            gift.setSeen(false);
            gift.setProduct(product);
            gift.setUseTypeOption(option);

            int gold = sender.getGold();
            int ap = ftClient.getAp().get();

            int costsGold = 0;
            int costsAp = 0;

            if (product.getPriceType().equals(PriceType.GOLD.getName())) {
                if (option <= 0)
                    costsGold = product.getPrice0();
                else if (option == 1)
                    costsGold = product.getPrice1();
                else
                    costsGold = product.getPrice2();
            }
            if (product.getPriceType().equals(PriceType.MINT.getName())) {
                if (option <= 0)
                    costsAp = product.getPrice0();
                else if (option == 1)
                    costsAp = product.getPrice1();
                else
                    costsAp = product.getPrice2();
            }

            AtomicInteger resultGold = new AtomicInteger(gold - costsGold);
            if (resultGold.get() < 0) {
                S2CSendGiftAnswerPacket s2CSendGiftAnswerPacket = new S2CSendGiftAnswerPacket(S2CShopBuyPacket.NEED_MORE_GOLD, null);
                connection.sendTCP(s2CSendGiftAnswerPacket);
                return;
            }

            int resultAp = ap - costsAp;
            if (resultAp < 0) {
                S2CSendGiftAnswerPacket s2CSendGiftAnswerPacket = new S2CSendGiftAnswerPacket(S2CShopBuyPacket.NEED_MORE_CASH, null);
                connection.sendTCP(s2CSendGiftAnswerPacket);
                return;
            }

            int quantity = switch (option) {
                case 0 -> product.getUse0() == 0 ? 1 : product.getUse0();
                case 1 -> product.getUse1();
                case 2 -> product.getUse2();
                default -> 1;
            };
            List<PlayerPocket> playerPocketList = inventoryService.addItem(
                    receiver.getId(), product.getProductIndex(), quantity,
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

            giftService.save(gift);

            int newGold = Math.max(resultGold.get(), 0);
            sender.syncGold(newGold);
            boolean success = ftClient.getAp().compareAndSet(ap, resultAp);
            playerService.setMoney(sender.getPlayer(), resultGold.get());
            if (!success) {
                log.warn("Failed to update AP for player {} ({}): expected {}, actual {}, new {}",
                        sender.getName(),
                        sender.getId(),
                        ap,
                        ftClient.getAp().get(),
                        resultAp);
            }
            account.setAp(resultAp);
            ftClient.saveAccount(account);

            S2CReceivedGiftNotificationPacket s2CReceivedGiftNotificationPacket = new S2CReceivedGiftNotificationPacket(gift);
            S2CInventoryItemsPlacePacket inventoryDataPacket = new S2CInventoryItemsPlacePacket(playerPocketList);

            PacketMessage packetMessage = PacketMessage.builder()
                    .receivingPlayerId(receiver.getId())
                    .packet(s2CReceivedGiftNotificationPacket)
                    .build();
            PacketMessage packetMessage2 = PacketMessage.builder()
                    .receivingPlayerId(receiver.getId())
                    .packet(inventoryDataPacket)
                    .build();
            rProducerService.send(packetMessage, "game.messenger.gift chat.messenger.gift", sender.getName() + "(ChatServer)");
            rProducerService.send(packetMessage2, "game.messenger.gift chat.messenger.gift", sender.getName() + "(ChatServer)");

            // 0 = Item purchase successful, -1 = Not enough gold, -2 = Not enough AP,
            // -3 = Receiver reached maximum number of character, -6 = That user already has the maximum number of this item
            // -8 = That users character model cannot equip this item,  -9 = You cannot send gifts purchases with gold to that character
            S2CSendGiftAnswerPacket s2CSendGiftAnswerPacket = new S2CSendGiftAnswerPacket(S2CShopBuyPacket.SUCCESS, gift);
            connection.sendTCP(s2CSendGiftAnswerPacket);

            SMSGSetMoney moneyPacket = SMSGSetMoney.builder()
                    .ap(ftClient.getAp().get())
                    .gold(sender.getGold())
                    .build();
            connection.sendTCP(moneyPacket);
        }
    }
}

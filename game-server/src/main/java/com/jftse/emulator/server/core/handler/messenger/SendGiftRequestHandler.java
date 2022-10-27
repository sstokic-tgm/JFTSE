package com.jftse.emulator.server.core.handler.messenger;

import com.jftse.emulator.server.core.packets.inventory.S2CInventoryDataPacket;
import com.jftse.emulator.server.core.packets.messenger.C2SSendGiftRequestPacket;
import com.jftse.emulator.server.core.packets.messenger.S2CReceivedGiftNotificationPacket;
import com.jftse.emulator.server.core.packets.messenger.S2CSendGiftAnswerPacket;
import com.jftse.emulator.server.core.packets.shop.S2CShopMoneyAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.item.EItemUseType;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.item.Product;
import com.jftse.entities.database.model.messenger.Gift;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.*;
import org.springframework.util.ReflectionUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

@PacketOperationIdentifier(PacketOperations.C2SSendGiftRequest)
public class SendGiftRequestHandler extends AbstractPacketHandler {
    private C2SSendGiftRequestPacket c2SSendGiftRequestPacket;

    private final PlayerService playerService;
    private final ProductService productService;
    private final PocketService pocketService;
    private final PlayerPocketService playerPocketService;
    private final GiftService giftService;

    public SendGiftRequestHandler() {
        playerService = ServiceManager.getInstance().getPlayerService();
        productService = ServiceManager.getInstance().getProductService();
        pocketService = ServiceManager.getInstance().getPocketService();
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        giftService = ServiceManager.getInstance().getGiftService();
    }

    @Override
    public boolean process(Packet packet) {
        c2SSendGiftRequestPacket = new C2SSendGiftRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient ftClient = connection.getClient();
        if (ftClient == null || ftClient.getPlayer() == null)
            return;

        byte option = c2SSendGiftRequestPacket.getOption();

        Product product = productService.findProductByProductItemIndex(c2SSendGiftRequestPacket.getProductIndex());
        Player sender = ftClient.getPlayer();
        Player receiver = playerService.findByName(c2SSendGiftRequestPacket.getReceiverName());

        if (receiver != null && product != null) {
            if (!product.getEnabled()) {
                S2CSendGiftAnswerPacket s2CSendGiftAnswerPacket = new S2CSendGiftAnswerPacket((short) -9, null);
                connection.sendTCP(s2CSendGiftAnswerPacket);
                return;
            }

            Gift gift = new Gift();
            gift.setReceiver(receiver);
            gift.setSender(sender);
            gift.setMessage(c2SSendGiftRequestPacket.getMessage());
            gift.setSeen(false);
            gift.setProduct(product);
            gift.setUseTypeOption(option);

            Integer currentGold = sender.getGold();
            Integer price = product.getPrice0();
            if (price > currentGold) {
                S2CSendGiftAnswerPacket s2CSendGiftAnswerPacket = new S2CSendGiftAnswerPacket((short) -1, null);
                connection.sendTCP(s2CSendGiftAnswerPacket);
                return;
            }

            List<PlayerPocket> playerPocketList = new ArrayList<>();

            Pocket receiverPocket = receiver.getPocket();
            if (!product.getCategory().equals(EItemCategory.CHAR.getName())) {
                if (!product.getCategory().equals(EItemCategory.HOUSE.getName())) {
                    // gold back
                    if (product.getGoldBack() != 0)
                        currentGold += product.getGoldBack();

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

                            Player newPlayer = productService.createNewPlayer(receiver.getAccount(), product.getForPlayer());
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
                                playerPocket.setPocket(receiverPocket);

                                playerPocket = playerPocketService.save(playerPocket);
                                receiverPocket = pocketService.incrementPocketBelongings(receiverPocket);

                                // add item to result
                                playerPocketList.add(playerPocket);
                            }
                        }
                    } else {
                        PlayerPocket playerPocket = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(product.getItem0(), product.getCategory(), receiverPocket);
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

                        if (option == 0)
                            playerPocket.setItemCount(product.getUse0() == 0 ? 1 : product.getUse0());
                        else if (option == 1)
                            playerPocket.setItemCount(product.getUse1());
                        else if (option == 2)
                            playerPocket.setItemCount(product.getUse2());

                        // no idea how itemCount can be null here, but ok
                        playerPocket.setItemCount((playerPocket.getItemCount() == null ? 0 : playerPocket.getItemCount()) + existingItemCount);

                        if (playerPocket.getUseType().equalsIgnoreCase(EItemUseType.TIME.getName())) {
                            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                            cal.add(Calendar.DAY_OF_MONTH, playerPocket.getItemCount());

                            playerPocket.setCreated(cal.getTime());
                        }
                        playerPocket.setPocket(receiverPocket);

                        playerPocket = playerPocketService.save(playerPocket);
                        if (!existingItem)
                            receiverPocket = pocketService.incrementPocketBelongings(receiverPocket);

                        // add item to result
                        playerPocketList.add(playerPocket);
                    }
                    receiver.setPocket(receiverPocket);
                    playerService.save(receiver);
                }
            }
            sender = playerService.setMoney(sender, currentGold - price);
            giftService.save(gift);
            ftClient.savePlayer(sender);

            FTClient receiverClient = GameManager.getInstance().getClients().stream()
                    .filter(x -> x.getPlayer() != null && x.getPlayer().getId().equals(receiver.getId()))
                    .findFirst()
                    .orElse(null);
            if (receiverClient != null) {
                S2CReceivedGiftNotificationPacket s2CReceivedGiftNotificationPacket = new S2CReceivedGiftNotificationPacket(gift);
                receiverClient.getConnection().sendTCP(s2CReceivedGiftNotificationPacket);

                S2CInventoryDataPacket inventoryDataPacket = new S2CInventoryDataPacket(playerPocketList);
                receiverClient.getConnection().sendTCP(inventoryDataPacket);
            }

            // 0 = Item purchase successful, -1 = Not enough gold, -2 = Not enough AP,
            // -3 = Receiver reached maximum number of character, -6 = That user already has the maximum number of this item
            // -8 = That users character model cannot equip this item,  -9 = You cannot send gifts purchases with gold to that character
            S2CSendGiftAnswerPacket s2CSendGiftAnswerPacket = new S2CSendGiftAnswerPacket((short) 0, gift);
            connection.sendTCP(s2CSendGiftAnswerPacket);

            S2CShopMoneyAnswerPacket senderMoneyPacket = new S2CShopMoneyAnswerPacket(sender);
            connection.sendTCP(senderMoneyPacket);
        }
    }
}

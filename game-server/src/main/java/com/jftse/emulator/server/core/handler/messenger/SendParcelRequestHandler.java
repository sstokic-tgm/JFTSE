package com.jftse.emulator.server.core.handler.messenger;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.messenger.S2CParcelListPacket;
import com.jftse.emulator.server.core.packets.messenger.S2CReceivedParcelNotificationPacket;
import com.jftse.emulator.server.core.rabbit.service.RProducerService;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.item.Product;
import com.jftse.entities.database.model.log.GameLog;
import com.jftse.entities.database.model.log.GameLogType;
import com.jftse.entities.database.model.messenger.EParcelType;
import com.jftse.entities.database.model.messenger.Parcel;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.*;
import com.jftse.server.core.shared.packets.inventory.S2CInventoryItemRemoveAnswerPacket;
import com.jftse.server.core.shared.packets.messenger.CMSGSendParcel;
import com.jftse.server.core.shared.packets.messenger.SMSGSendParcel;
import com.jftse.server.core.shared.rabbit.messages.PacketMessage;

import java.util.List;

@PacketId(CMSGSendParcel.PACKET_ID)
public class SendParcelRequestHandler implements PacketHandler<FTConnection, CMSGSendParcel> {
    private final PlayerService playerService;
    private final ProductService productService;
    private final PlayerPocketService playerPocketService;
    private final ParcelService parcelService;

    private final GameLogService gameLogService;

    private final RProducerService rProducerService;

    public SendParcelRequestHandler() {
        playerService = ServiceManager.getInstance().getPlayerService();
        productService = ServiceManager.getInstance().getProductService();
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        parcelService = ServiceManager.getInstance().getParcelService();
        gameLogService = ServiceManager.getInstance().getGameLogService();
        rProducerService = RProducerService.getInstance();
    }

    @Override
    public void handle(FTConnection connection, CMSGSendParcel packet) {
        FTClient ftClient = connection.getClient();
        if (ftClient == null)
            return;

        PlayerPocket item = playerPocketService.findById((long) packet.getPlayerPocketId());
        if (item == null) {
            SMSGSendParcel response = SMSGSendParcel.builder().status((short) -1).build();
            connection.sendTCP(response);
            return;
        }

        if (packet.getCashOnDelivery() > 1000000) {
            SMSGSendParcel response = SMSGSendParcel.builder().status((short) -5).build();
            connection.sendTCP(response);
            return;
        }

        Product product = productService.findProductByItemAndCategoryAndEnabledIsTrue(item.getItemIndex(), item.getCategory());

        if (product != null && !product.getEnableParcel()) {
            SMSGSendParcel response = SMSGSendParcel.builder().status((short) -1).build();
            connection.sendTCP(response);
        } else {
            Player sender = ftClient.getPlayer();
            if (sender == null) {
                SMSGSendParcel response = SMSGSendParcel.builder().status((short) -1).build();
                connection.sendTCP(response);
                return;
            }

            if (!sender.getPocket().getId().equals(item.getPocket().getId())) {
                SMSGSendParcel response = SMSGSendParcel.builder().status((short) -1).build();
                connection.sendTCP(response);

                GameLog gameLog = new GameLog();
                gameLog.setGameLogType(GameLogType.BANABLE);
                gameLog.setContent("pockets are not equal! requested pocketId: " + item.getPocket().getId() + ", requested playerPocketId: " + item.getId() + ", requesting player pocketId: " + sender.getPocket().getId() + ", requesting playerId: " + sender.getId());
                gameLogService.save(gameLog);

                return;
            }

            if (sender.getLevel() < 20) {
                SMSGSendParcel response = SMSGSendParcel.builder().status((short) -4).build();
                connection.sendTCP(response);
            } else {
                Player receiver = playerService.findByName(packet.getReceiverName());
                if (receiver != null) {
                    if (receiver.getLevel() < 20) {
                        SMSGSendParcel response = SMSGSendParcel.builder().status((short) -4).build();
                        connection.sendTCP(response);
                        return;
                    }

                    List<Parcel> receiverParcels = parcelService.findByReceiver(receiver);
                    List<Parcel> senderParcels = parcelService.findBySender(sender);
                    if (receiverParcels.size() > 128 || senderParcels.size() > 128) {
                        SMSGSendParcel response = SMSGSendParcel.builder().status((short) -1).build();
                        connection.sendTCP(response);
                        return;
                    }

                    // TODO: Parcels should have a retention of 7days. -> After 7 days delete parcels and return items back to senders pocket.
                    Parcel parcel = new Parcel();
                    parcel.setReceiver(receiver);
                    parcel.setSender(sender);
                    parcel.setMessage(packet.getMessage());
                    parcel.setGold(packet.getCashOnDelivery());

                    parcel.setItemCount(item.getItemCount());
                    parcel.setCategory(item.getCategory());
                    parcel.setItemIndex(item.getItemIndex());
                    parcel.setUseType(item.getUseType());
                    parcel.setEnchantStr(item.getEnchantStr());
                    parcel.setEnchantSta(item.getEnchantSta());
                    parcel.setEnchantDex(item.getEnchantDex());
                    parcel.setEnchantWil(item.getEnchantWil());
                    parcel.setEnchantElement(item.getEnchantElement());
                    parcel.setEnchantLevel(item.getEnchantLevel());

                    // TODO: Is this right?
                    if (receiver.getId().equals(sender.getId())) {
                        parcel.setEParcelType(EParcelType.Gold);
                    } else {
                        parcel.setEParcelType(EParcelType.CashOnDelivery);
                    }

                    sender = playerService.updateMoney(sender, -30); // fee 30 gold
                    if (sender.getGold() < 0) {
                        playerService.updateMoney(sender, 30);
                        SMSGSendParcel response = SMSGSendParcel.builder().status((short) -2).build();
                        connection.sendTCP(response);
                        return;
                    }

                    if (parcel.getEParcelType().equals(EParcelType.Gold)) {
                        final int newGold = sender.getGold() - parcel.getGold();
                        if (newGold < 0) {
                            SMSGSendParcel response = SMSGSendParcel.builder().status((short) -2).build();
                            connection.sendTCP(response);
                            return;
                        }
                    }

                    playerService.save(sender);
                    parcelService.save(parcel);
                    playerPocketService.remove(item.getId());

                    S2CReceivedParcelNotificationPacket s2CReceivedParcelNotificationPacket = new S2CReceivedParcelNotificationPacket(parcel);

                    PacketMessage packetMessage = PacketMessage.builder()
                            .receivingPlayerId(receiver.getId())
                            .packet(s2CReceivedParcelNotificationPacket)
                            .build();
                    rProducerService.send(packetMessage, "game.messenger.parcel chat.messenger.parcel", sender.getName() + "(GameServer)");

                    // TODO: Handle fee
                    // TODO: Handle all these cases
                    // 0 = Successfully sent
                    //-1 = Failed to send parcel
                    //-2 = You do not have enough gold
                    //-4 = Under level 20 user can not send parcel
                    //-5 = Gold transactions must be under 1.000.000
                    SMSGSendParcel response = SMSGSendParcel.builder().status((short) 0).build();
                    connection.sendTCP(response);

                    S2CInventoryItemRemoveAnswerPacket s2CInventoryItemRemoveAnswerPacket = new S2CInventoryItemRemoveAnswerPacket(item.getId().intValue());
                    connection.sendTCP(s2CInventoryItemRemoveAnswerPacket);

                    List<Parcel> sentParcels = parcelService.findBySender(parcel.getSender());
                    S2CParcelListPacket s2CSentParcelListPacket = new S2CParcelListPacket((byte) 1, sentParcels);
                    connection.sendTCP(s2CSentParcelListPacket);
                }
            }
        }
    }
}

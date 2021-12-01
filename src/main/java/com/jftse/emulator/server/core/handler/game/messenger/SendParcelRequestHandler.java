package com.jftse.emulator.server.core.handler.game.messenger;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.inventory.S2CInventoryItemRemoveAnswerPacket;
import com.jftse.emulator.server.core.packet.packets.messenger.C2SSendParcelRequestPacket;
import com.jftse.emulator.server.core.packet.packets.messenger.S2CParcelListPacket;
import com.jftse.emulator.server.core.packet.packets.messenger.S2CReceivedParcelNotificationPacket;
import com.jftse.emulator.server.core.packet.packets.messenger.S2CSendParcelAnswerPacket;
import com.jftse.emulator.server.core.service.PlayerPocketService;
import com.jftse.emulator.server.core.service.PlayerService;
import com.jftse.emulator.server.core.service.ProductService;
import com.jftse.emulator.server.core.service.messenger.ParcelService;
import com.jftse.emulator.server.database.model.item.Product;
import com.jftse.emulator.server.database.model.messenger.EParcelType;
import com.jftse.emulator.server.database.model.messenger.Parcel;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.database.model.pocket.PlayerPocket;
import com.jftse.emulator.server.networking.packet.Packet;
import com.jftse.emulator.server.shared.module.Client;

import java.util.List;

public class SendParcelRequestHandler extends AbstractHandler {
    private C2SSendParcelRequestPacket c2SSendParcelRequestPacket;

    private final PlayerService playerService;
    private final ProductService productService;
    private final PlayerPocketService playerPocketService;
    private final ParcelService parcelService;

    public SendParcelRequestHandler() {
        playerService = ServiceManager.getInstance().getPlayerService();
        productService = ServiceManager.getInstance().getProductService();
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        parcelService = ServiceManager.getInstance().getParcelService();
    }

    @Override
    public boolean process(Packet packet) {
        c2SSendParcelRequestPacket = new C2SSendParcelRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null || connection.getClient().getActivePlayer() == null)
            return;

        PlayerPocket item = playerPocketService.findById(c2SSendParcelRequestPacket.getPlayerPocketId().longValue());
        Product product = productService.findProductByItemAndCategoryAndEnabledIsTrue(item.getItemIndex(), item.getCategory());

        if (product != null && !product.getEnableParcel()) {
            S2CSendParcelAnswerPacket s2CSendParcelAnswerPacket = new S2CSendParcelAnswerPacket((short) -1);
            connection.sendTCP(s2CSendParcelAnswerPacket);
        } else {
            Player sender = playerService.findById(connection.getClient().getActivePlayer().getId());
            Player receiver = playerService.findByName(c2SSendParcelRequestPacket.getReceiverName());
            if (receiver != null && item != null) {
                if (item != null) {
                    // TODO: Parcels should have a retention of 7days. -> After 7 days delete parcels and return items back to senders pocket.
                    Parcel parcel = new Parcel();
                    parcel.setReceiver(receiver);
                    parcel.setSender(connection.getClient().getActivePlayer());
                    parcel.setMessage(c2SSendParcelRequestPacket.getMessage());
                    parcel.setGold(c2SSendParcelRequestPacket.getCashOnDelivery());

                    parcel.setItemCount(item.getItemCount());
                    parcel.setCategory(item.getCategory());
                    parcel.setItemIndex(item.getItemIndex());
                    parcel.setUseType(item.getUseType());

                    // TODO: Is this right?
                    if (receiver.getId().equals(sender.getId())) {
                        parcel.setEParcelType(EParcelType.Gold);
                    } else {
                        parcel.setEParcelType(EParcelType.CashOnDelivery);
                    }

                    parcelService.save(parcel);
                    playerPocketService.remove(item.getId());

                    Client receiverClient = GameManager.getInstance().getClients().stream()
                            .filter(x -> x.getActivePlayer().getId().equals(receiver.getId()))
                            .findFirst()
                            .orElse(null);
                    if (receiverClient != null) {
                        S2CReceivedParcelNotificationPacket s2CReceivedParcelNotificationPacket = new S2CReceivedParcelNotificationPacket(parcel);
                        connection.getServer().sendToTcp(receiverClient.getConnection().getId(), s2CReceivedParcelNotificationPacket);
                    }

                    // TODO: Handle fee
                    // TODO: Handle all these cases
                    // 0 = Successfully sent
                    //-1 = Failed to send parcel
                    //-2 = You do not have enough gold
                    //-4 = Under level 20 user can not send parcel
                    //-5 = Gold transactions must be under 1.000.000
                    S2CSendParcelAnswerPacket s2CSendParcelAnswerPacket = new S2CSendParcelAnswerPacket((short) 0);
                    connection.sendTCP(s2CSendParcelAnswerPacket);

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

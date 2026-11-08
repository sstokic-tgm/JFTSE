package com.jftse.emulator.server.core.handler.messenger;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryItemsPlacePacket;
import com.jftse.emulator.server.core.rabbit.service.RProducerService;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.ParcelTransactionService;
import com.jftse.server.core.service.transaction.ParcelTransactionResult;
import com.jftse.server.core.service.transaction.TransactionException;
import com.jftse.server.core.service.transaction.TransactionNotifications;
import com.jftse.server.core.shared.packets.messenger.CMSGDenyParcel;
import com.jftse.server.core.shared.packets.messenger.SMSGDeleteParcel;
import com.jftse.server.core.shared.rabbit.messages.PacketMessage;

import java.util.List;

@PacketId(CMSGDenyParcel.PACKET_ID)
public class DenyParcelRequestHandler implements PacketHandler<FTConnection, CMSGDenyParcel> {
    private final ParcelTransactionService parcelTransactionService;
    private final RProducerService rProducerService;

    public DenyParcelRequestHandler() {
        parcelTransactionService = ServiceManager.getInstance().getParcelTransactionService();
        rProducerService = RProducerService.getInstance();
    }

    @Override
    public void handle(FTConnection connection, CMSGDenyParcel packet) {
        FTClient client = connection.getClient();
        if (client == null || !client.hasPlayer()) {
            return;
        }

        ParcelTransactionResult result;
        try {
            result = parcelTransactionService.deny(client.getAccountId(), client.getPlayer().getId(), (long) packet.getParcelId());
        } catch (TransactionException | ArithmeticException exception) {
            return;
        }

        TransactionNotifications.afterCommit(() -> {
            SMSGDeleteParcel deletePacket = SMSGDeleteParcel.builder()
                    .parcelId(result.parcel().getId().intValue())
                    .build();
            connection.sendTCP(deletePacket);

            S2CInventoryItemsPlacePacket inventoryPacket = new S2CInventoryItemsPlacePacket(List.of(result.item()));
            PacketMessage message = PacketMessage.builder()
                    .receivingPlayerId(result.parcel().getSender().getId())
                    .packet(inventoryPacket)
                    .build();
            rProducerService.send(message, "game.messenger.parcel chat.messenger.parcel",
                    result.parcel().getReceiver().getName() + "(ChatServer)");
        });
    }
}

package com.jftse.emulator.server.core.handler.messenger;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryItemsPlacePacket;
import com.jftse.emulator.server.core.rabbit.service.RProducerService;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.service.ParcelTransactionService;
import com.jftse.server.core.service.transaction.ParcelTransactionResult;
import com.jftse.server.core.service.transaction.TransactionException;
import com.jftse.server.core.service.transaction.TransactionNotifications;
import com.jftse.server.core.shared.packets.messenger.CMSGCancelParcel;
import com.jftse.server.core.shared.packets.messenger.SMSGCancelParcel;
import com.jftse.server.core.shared.packets.messenger.SMSGDeleteParcel;
import com.jftse.server.core.shared.rabbit.messages.PacketMessage;

import java.util.List;

@PacketId(CMSGCancelParcel.PACKET_ID)
public class CancelSendingParcelRequestHandler implements PacketHandler<FTConnection, CMSGCancelParcel> {
    private final ParcelTransactionService parcelTransactionService;
    private final RProducerService rProducerService;

    public CancelSendingParcelRequestHandler() {
        parcelTransactionService = ServiceManager.getInstance().getParcelTransactionService();
        rProducerService = RProducerService.getInstance();
    }

    @Override
    public void handle(FTConnection connection, CMSGCancelParcel packet) {
        FTClient client = connection.getClient();
        if (client == null || !client.hasPlayer()) {
            return;
        }

        ParcelTransactionResult result;
        try {
            result = parcelTransactionService.cancel(client.getAccountId(), client.getPlayer().getId(), (long) packet.getParcelId());
        } catch (TransactionException exception) {
            if (exception.getReason() != TransactionException.Reason.NOT_FOUND) {
                connection.sendTCP(SMSGCancelParcel.builder().status((short) -1).build());
            }
            return;
        } catch (ArithmeticException exception) {
            connection.sendTCP(SMSGCancelParcel.builder().status((short) -1).build());
            return;
        }

        TransactionNotifications.afterCommit(() -> {
            connection.sendTCP(new S2CInventoryItemsPlacePacket(List.of(result.item())));
            connection.sendTCP(SMSGCancelParcel.builder().status((short) 0).build());

            SMSGDeleteParcel deletePacket = SMSGDeleteParcel.builder()
                    .parcelId(result.parcel().getId().intValue())
                    .build();
            PacketMessage message = PacketMessage.builder()
                    .receivingPlayerId(result.parcel().getReceiver().getId())
                    .packet(new Packet(deletePacket))
                    .build();
            rProducerService.send(message, "game.messenger.parcel chat.messenger.parcel",
                    result.parcel().getSender().getName() + "(GameServer)");
        });
    }
}

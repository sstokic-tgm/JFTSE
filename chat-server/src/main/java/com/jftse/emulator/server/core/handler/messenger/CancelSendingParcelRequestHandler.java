package com.jftse.emulator.server.core.handler.messenger;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryItemsPlacePacket;
import com.jftse.emulator.server.core.rabbit.service.RProducerService;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.messenger.Parcel;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.service.ParcelService;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.shared.packets.messenger.CMSGCancelParcel;
import com.jftse.server.core.shared.packets.messenger.SMSGCancelParcel;
import com.jftse.server.core.shared.packets.messenger.SMSGDeleteParcel;
import com.jftse.server.core.shared.rabbit.messages.PacketMessage;

import java.util.List;

@PacketId(CMSGCancelParcel.PACKET_ID)
public class CancelSendingParcelRequestHandler implements PacketHandler<FTConnection, CMSGCancelParcel> {
    private final ParcelService parcelService;
    private final PlayerPocketService playerPocketService;

    private final RProducerService rProducerService;

    public CancelSendingParcelRequestHandler() {
        parcelService = ServiceManager.getInstance().getParcelService();
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        rProducerService = RProducerService.getInstance();
    }

    @Override
    public void handle(FTConnection connection, CMSGCancelParcel packet) {
        Parcel parcel = parcelService.findById((long) packet.getParcelId());
        if (parcel == null) return;

        PlayerPocket item = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(parcel.getItemIndex(), parcel.getCategory(), parcel.getSender().getPocket());
        if (item == null) {
            item = new PlayerPocket();
            item.setCategory(parcel.getCategory());
            item.setItemCount(parcel.getItemCount());
            item.setItemIndex(parcel.getItemIndex());
            item.setUseType(parcel.getUseType());
            item.setPocket(parcel.getSender().getPocket());
            item.setEnchantStr(parcel.getEnchantStr());
            item.setEnchantSta(parcel.getEnchantSta());
            item.setEnchantDex(parcel.getEnchantDex());
            item.setEnchantWil(parcel.getEnchantWil());
            item.setEnchantElement(parcel.getEnchantElement());
            item.setEnchantLevel(parcel.getEnchantLevel());
        } else {
            item.setItemCount(item.getItemCount() + parcel.getItemCount());
        }

        item = playerPocketService.save(item);
        parcelService.remove(parcel.getId());

        S2CInventoryItemsPlacePacket s2CInventoryItemsPlacePacket = new S2CInventoryItemsPlacePacket(List.of(item));
        connection.sendTCP(s2CInventoryItemsPlacePacket);

        SMSGCancelParcel answer = SMSGCancelParcel.builder().status((short) 0).build();
        connection.sendTCP(answer);

        SMSGDeleteParcel answerPacket = SMSGDeleteParcel.builder().parcelId(parcel.getId().intValue()).build();

        PacketMessage packetMessage = PacketMessage.builder()
                .receivingPlayerId(parcel.getReceiver().getId())
                .packet(new Packet(answerPacket))
                .build();
        rProducerService.send(packetMessage, "game.messenger.parcel chat.messenger.parcel", parcel.getSender().getName() + "(ChatServer)");
    }
}

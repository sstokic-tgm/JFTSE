package com.jftse.emulator.server.core.handler.messenger;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryItemsPlacePacket;
import com.jftse.emulator.server.core.rabbit.service.RProducerService;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.messenger.Parcel;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.ParcelService;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.shared.packets.messenger.CMSGDenyParcel;
import com.jftse.server.core.shared.packets.messenger.SMSGDeleteParcel;
import com.jftse.server.core.shared.rabbit.messages.PacketMessage;

import java.util.List;

@PacketId(CMSGDenyParcel.PACKET_ID)
public class DenyParcelRequestHandler implements PacketHandler<FTConnection, CMSGDenyParcel> {
    private final ParcelService parcelService;
    private final PlayerPocketService playerPocketService;

    private final RProducerService rProducerService;

    public DenyParcelRequestHandler() {
        parcelService = ServiceManager.getInstance().getParcelService();
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        rProducerService = RProducerService.getInstance();
    }

    @Override
    public void handle(FTConnection connection, CMSGDenyParcel packet) {
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

        SMSGDeleteParcel answer = SMSGDeleteParcel.builder().parcelId(parcel.getId().intValue()).build();
        connection.sendTCP(answer);

        S2CInventoryItemsPlacePacket s2CInventoryItemsPlacePacket = new S2CInventoryItemsPlacePacket(List.of(item));

        PacketMessage packetMessage = PacketMessage.builder()
                .receivingPlayerId(parcel.getSender().getId())
                .packet(s2CInventoryItemsPlacePacket)
                .build();
        rProducerService.send(packetMessage, "game.messenger.parcel chat.messenger.parcel", parcel.getReceiver().getName() + "(GameServer)");
    }
}

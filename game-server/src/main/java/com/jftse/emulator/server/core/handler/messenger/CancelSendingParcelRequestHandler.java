package com.jftse.emulator.server.core.handler.messenger;

import com.jftse.emulator.server.core.packets.inventory.S2CInventoryDataPacket;
import com.jftse.emulator.server.core.packets.messenger.C2SCancelParcelSendingRequest;
import com.jftse.emulator.server.core.packets.messenger.S2CCancelParcelSendingAnswer;
import com.jftse.emulator.server.core.packets.messenger.S2CRemoveParcelFromListPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.messenger.Parcel;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.ParcelService;
import com.jftse.server.core.service.PlayerPocketService;

import java.util.List;

@PacketOperationIdentifier(PacketOperations.C2SCancelParcelSendingRequest)
public class CancelSendingParcelRequestHandler extends AbstractPacketHandler {
    private C2SCancelParcelSendingRequest c2SCancelParcelSendingRequest;

    private final ParcelService parcelService;
    private final PlayerPocketService playerPocketService;

    public CancelSendingParcelRequestHandler() {
        parcelService = ServiceManager.getInstance().getParcelService();
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
    }

    @Override
    public boolean process(Packet packet) {
        c2SCancelParcelSendingRequest = new C2SCancelParcelSendingRequest(packet);
        return true;
    }

    @Override
    public void handle() {
        Parcel parcel = parcelService.findById(c2SCancelParcelSendingRequest.getParcelId().longValue());
        if (parcel == null) return;

        PlayerPocket item = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(parcel.getItemIndex(), parcel.getCategory(), parcel.getSender().getPocket());
        if (item == null) {
            item = new PlayerPocket();
            item.setCategory(parcel.getCategory());
            item.setItemCount(parcel.getItemCount());
            item.setItemIndex(parcel.getItemIndex());
            item.setUseType(parcel.getUseType());
            item.setPocket(parcel.getSender().getPocket());
        } else {
            item.setItemCount(item.getItemCount() + parcel.getItemCount());
        }

        playerPocketService.save(item);
        parcelService.remove(parcel.getId());

        List<PlayerPocket> items = playerPocketService.getPlayerPocketItems(parcel.getSender().getPocket());
        S2CInventoryDataPacket s2CInventoryDataPacket = new S2CInventoryDataPacket(items);
        connection.sendTCP(s2CInventoryDataPacket);

        S2CCancelParcelSendingAnswer s2CCancelParcelSendingAnswer = new S2CCancelParcelSendingAnswer((short) 0);
        connection.sendTCP(s2CCancelParcelSendingAnswer);

        FTClient receiverClient = GameManager.getInstance().getClients().stream()
                .filter(x -> x.getPlayer() != null && x.getPlayer().getId().equals(parcel.getReceiver().getId()))
                .findFirst()
                .orElse(null);
        if (receiverClient != null) {
            S2CRemoveParcelFromListPacket s2CRemoveParcelFromListPacket = new S2CRemoveParcelFromListPacket(parcel.getId().intValue());
            receiverClient.getConnection().sendTCP(s2CRemoveParcelFromListPacket);
        }
    }
}

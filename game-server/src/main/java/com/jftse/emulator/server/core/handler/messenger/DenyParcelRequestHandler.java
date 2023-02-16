package com.jftse.emulator.server.core.handler.messenger;

import com.jftse.emulator.server.core.packets.inventory.S2CInventoryDataPacket;
import com.jftse.emulator.server.core.packets.messenger.C2SDenyParcelRequest;
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

@PacketOperationIdentifier(PacketOperations.C2SDenyParcelRequest)
public class DenyParcelRequestHandler extends AbstractPacketHandler {
    private C2SDenyParcelRequest c2SDenyParcelRequest;

    private final ParcelService parcelService;
    private final PlayerPocketService playerPocketService;

    public DenyParcelRequestHandler() {
        parcelService = ServiceManager.getInstance().getParcelService();
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
    }

    @Override
    public boolean process(Packet packet) {
        c2SDenyParcelRequest = new C2SDenyParcelRequest(packet);
        return true;
    }

    @Override
    public void handle() {
        Parcel parcel = parcelService.findById(c2SDenyParcelRequest.getParcelId().longValue());
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

        S2CRemoveParcelFromListPacket s2CRemoveParcelFromListPacket = new S2CRemoveParcelFromListPacket(parcel.getId().intValue());
        connection.sendTCP(s2CRemoveParcelFromListPacket);

        List<PlayerPocket> items = playerPocketService.getPlayerPocketItems(parcel.getSender().getPocket());
        FTClient senderClient = GameManager.getInstance().getClients().stream()
                .filter(x -> x.getPlayer() != null && x.getPlayer().getId().equals(parcel.getSender().getId()))
                .findFirst()
                .orElse(null);
        if (senderClient != null) {
            S2CInventoryDataPacket s2CInventoryDataPacket = new S2CInventoryDataPacket(items);
            senderClient.getConnection().sendTCP(s2CInventoryDataPacket);

            // TODO: Remove parcel from sent list of sender, S2CSentParcelListPacket doesn't work
        }
    }
}

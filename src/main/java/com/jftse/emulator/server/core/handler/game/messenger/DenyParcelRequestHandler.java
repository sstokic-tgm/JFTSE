package com.jftse.emulator.server.core.handler.game.messenger;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.inventory.S2CInventoryDataPacket;
import com.jftse.emulator.server.core.packet.packets.messenger.C2SDenyParcelRequest;
import com.jftse.emulator.server.core.packet.packets.messenger.S2CRemoveParcelFromListPacket;
import com.jftse.emulator.server.core.service.PlayerPocketService;
import com.jftse.emulator.server.core.service.messenger.ParcelService;
import com.jftse.emulator.server.database.model.messenger.Parcel;
import com.jftse.emulator.server.database.model.pocket.PlayerPocket;
import com.jftse.emulator.server.networking.packet.Packet;
import com.jftse.emulator.server.shared.module.Client;

import java.util.List;

public class DenyParcelRequestHandler extends AbstractHandler {
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
        Client senderClient = GameManager.getInstance().getClients().stream()
                .filter(x -> x.getActivePlayer().getId().equals(parcel.getSender().getId()))
                .findFirst()
                .orElse(null);
        if (senderClient != null) {
            S2CInventoryDataPacket s2CInventoryDataPacket = new S2CInventoryDataPacket(items);
            connection.getServer().sendToTcp(senderClient.getConnection().getId(), s2CInventoryDataPacket);

            // TODO: Remove parcel from sent list of sender, S2CSentParcelListPacket doesn't work
        }
    }
}

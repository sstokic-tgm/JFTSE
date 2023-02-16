package com.jftse.emulator.server.core.handler.game.messenger;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.messenger.C2SParcelListRequestPacket;
import com.jftse.emulator.server.core.packet.packets.messenger.S2CParcelListPacket;
import com.jftse.emulator.server.core.service.messenger.ParcelService;
import com.jftse.entities.database.model.messenger.Parcel;
import com.jftse.entities.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.ArrayList;
import java.util.List;

public class ParcelListRequestHandler extends AbstractHandler {
    private C2SParcelListRequestPacket parcelListRequestPacket;

    private final ParcelService parcelService;

    public ParcelListRequestHandler() {
        parcelService = ServiceManager.getInstance().getParcelService();
    }

    @Override
    public boolean process(Packet packet) {
        parcelListRequestPacket = new C2SParcelListRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null || connection.getClient().getPlayer() == null)
            return;

        byte listType = parcelListRequestPacket.getListType();

        Player player = connection.getClient().getPlayer();

        List<Parcel> parcelList = new ArrayList<>();
        switch (listType) {
            case 0 -> parcelList.addAll(parcelService.findByReceiver(player));
            case 1 -> parcelList.addAll(parcelService.findBySender(player));
        }

        S2CParcelListPacket s2CReceivedParcelListPacket = new S2CParcelListPacket(listType, parcelList);
        connection.sendTCP(s2CReceivedParcelListPacket);
    }
}

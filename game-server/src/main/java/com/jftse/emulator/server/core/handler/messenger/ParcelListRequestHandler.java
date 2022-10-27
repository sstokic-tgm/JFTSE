package com.jftse.emulator.server.core.handler.messenger;

import com.jftse.emulator.server.core.packets.messenger.C2SParcelListRequestPacket;
import com.jftse.emulator.server.core.packets.messenger.S2CParcelListPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.messenger.Parcel;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.ParcelService;

import java.util.ArrayList;
import java.util.List;

@PacketOperationIdentifier(PacketOperations.C2SParcelListRequest)
public class ParcelListRequestHandler extends AbstractPacketHandler {
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
        FTClient ftClient = connection.getClient();
        if (ftClient == null || ftClient.getPlayer() == null)
            return;

        byte listType = parcelListRequestPacket.getListType();

        Player player = ftClient.getPlayer();

        List<Parcel> parcelList = new ArrayList<>();
        switch (listType) {
            case 0 -> parcelList.addAll(parcelService.findByReceiver(player));
            case 1 -> parcelList.addAll(parcelService.findBySender(player));
        }

        S2CParcelListPacket s2CReceivedParcelListPacket = new S2CParcelListPacket(listType, parcelList);
        connection.sendTCP(s2CReceivedParcelListPacket);
    }
}

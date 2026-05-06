package com.jftse.emulator.server.core.handler.messenger;

import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.messenger.S2CParcelListPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.messenger.Parcel;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.ParcelService;
import com.jftse.server.core.shared.packets.messenger.CMSGParcelList;

import java.util.ArrayList;
import java.util.List;

@PacketId(CMSGParcelList.PACKET_ID)
public class ParcelListRequestHandler implements PacketHandler<FTConnection, CMSGParcelList> {
    private final ParcelService parcelService;

    public ParcelListRequestHandler() {
        parcelService = ServiceManager.getInstance().getParcelService();
    }

    @Override
    public void handle(FTConnection connection, CMSGParcelList parcelListRequestPacket) {
        FTClient ftClient = connection.getClient();
        if (!ftClient.hasPlayer())
            return;

        byte listType = parcelListRequestPacket.getListType();

        FTPlayer player = ftClient.getPlayer();

        List<Parcel> parcelList = new ArrayList<>();
        switch (listType) {
            case 0 -> parcelList.addAll(parcelService.findWithPlayerByReceiver(player.getId()));
            case 1 -> parcelList.addAll(parcelService.findWithPlayerBySender(player.getId()));
        }

        S2CParcelListPacket s2CReceivedParcelListPacket = new S2CParcelListPacket(listType, parcelList);
        connection.sendTCP(s2CReceivedParcelListPacket);
    }
}

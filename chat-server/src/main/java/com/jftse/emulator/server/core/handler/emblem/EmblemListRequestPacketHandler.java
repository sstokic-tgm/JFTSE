package com.jftse.emulator.server.core.handler.emblem;

import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.emblem.CMSGEmblemList;

@PacketId(CMSGEmblemList.PACKET_ID)
public class EmblemListRequestPacketHandler implements PacketHandler<FTConnection, CMSGEmblemList> {
    @Override
    public void handle(FTConnection connection, CMSGEmblemList packet) {
        // empty..
    }
}

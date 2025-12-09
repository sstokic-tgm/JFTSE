package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.CMSGUnknown0x1071;

@PacketId(CMSGUnknown0x1071.PACKET_ID)
public class Unknown0x1071PacketHandler implements PacketHandler<FTConnection, CMSGUnknown0x1071> {
    @Override
    public void handle(FTConnection connection, CMSGUnknown0x1071 packet) {
        // empty
    }
}

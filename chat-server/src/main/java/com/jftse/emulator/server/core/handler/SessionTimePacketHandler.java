package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.CMSGSessionTime;

@PacketId(CMSGSessionTime.PACKET_ID)
public class SessionTimePacketHandler implements PacketHandler<FTConnection, CMSGSessionTime> {
    @Override
    public void handle(FTConnection connection, CMSGSessionTime packet) {
        // empty
    }
}

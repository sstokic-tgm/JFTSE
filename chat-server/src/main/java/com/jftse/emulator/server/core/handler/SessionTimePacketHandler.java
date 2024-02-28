package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.server.core.packets.C2SSessionTimePacket;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

@PacketOperationIdentifier(PacketOperations.C2SSessionTime)
public class SessionTimePacketHandler extends AbstractPacketHandler {
    private C2SSessionTimePacket sessionTimePacket;

    @Override
    public boolean process(Packet packet) {
        sessionTimePacket = new C2SSessionTimePacket(packet);
        return true;
    }

    @Override
    public void handle() {

    }
}

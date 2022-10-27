package com.jftse.emulator.server.core.handler;

import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

@PacketOperationIdentifier(PacketOperations.C2SUnknown0x1071)
public class Unknown0x1071PacketHandler extends AbstractPacketHandler {
    private Packet packet;
    @Override
    public boolean process(Packet packet) {
        this.packet = packet;
        return true;
    }

    @Override
    public void handle() {
        Packet answer = new Packet(packet.getRawPacket());
        connection.sendTCP(answer);
    }
}

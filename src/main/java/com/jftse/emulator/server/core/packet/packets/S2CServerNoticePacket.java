package com.jftse.emulator.server.core.packet.packets;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CServerNoticePacket extends Packet {
    public S2CServerNoticePacket(String message) {
        super(PacketOperations.S2CServerNotice.getValueAsChar());

        this.write(message);
    }
}
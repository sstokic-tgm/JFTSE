package com.jftse.emulator.server.core.packet.packets;

import com.jftse.emulator.server.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CDisconnectAnswerPacket extends Packet {
    public S2CDisconnectAnswerPacket() {
        super(PacketID.S2CDisconnectAnswer);

        this.write((byte) 0);
    }
}

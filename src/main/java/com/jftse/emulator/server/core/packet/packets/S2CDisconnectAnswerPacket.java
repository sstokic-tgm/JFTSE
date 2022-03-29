package com.jftse.emulator.server.core.packet.packets;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CDisconnectAnswerPacket extends Packet {
    public S2CDisconnectAnswerPacket() {
        super(PacketOperations.S2CDisconnectAnswer.getValueAsChar());

        this.write((byte) 0);
    }
}

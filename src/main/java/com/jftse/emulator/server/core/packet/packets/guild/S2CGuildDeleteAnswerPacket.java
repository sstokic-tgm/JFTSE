package com.jftse.emulator.server.core.packet.packets.guild;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CGuildDeleteAnswerPacket extends Packet {
    public S2CGuildDeleteAnswerPacket(short result) {
        super(PacketOperations.S2CGuildDeleteAnswer.getValueAsChar());

        this.write(result);
    }
}
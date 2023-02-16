package com.jftse.emulator.server.core.packet.packets.guild;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CGuildNameCheckAnswerPacket extends Packet {
    public S2CGuildNameCheckAnswerPacket(short result) {
        super(PacketOperations.S2CGuildNameCheckAnswer.getValueAsChar());

        this.write(result);
    }
}

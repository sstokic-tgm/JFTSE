package com.jftse.emulator.server.core.packet.packets.guild;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CGuildCreateAnswerPacket extends Packet {
    public S2CGuildCreateAnswerPacket(char result) {
        super(PacketOperations.S2CGuildCreateAnswer.getValueAsChar());

        this.write(result);
    }
}

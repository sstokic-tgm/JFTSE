package com.jftse.emulator.server.core.packet.packets.guild;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CGuildChangeLogoAnswerPacket extends Packet {
    public S2CGuildChangeLogoAnswerPacket(short result) {
        super(PacketOperations.S2CGuildChangeLogoAnswer.getValueAsChar());

        this.write(result);
    }
}
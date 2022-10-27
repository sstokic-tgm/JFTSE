package com.jftse.emulator.server.core.packets.guild;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CGuildChangeLogoAnswerPacket extends Packet {
    public S2CGuildChangeLogoAnswerPacket(short result) {
        super(PacketOperations.S2CGuildChangeLogoAnswer.getValue());

        this.write(result);
    }
}
package com.jftse.emulator.server.core.packet.packets.guild;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CGuildDismissMemberAnswerPacket extends Packet {
    public S2CGuildDismissMemberAnswerPacket(short result) {
        super(PacketOperations.S2CGuildDismissMemberAnswer.getValueAsChar());

        this.write(result);
    }
}

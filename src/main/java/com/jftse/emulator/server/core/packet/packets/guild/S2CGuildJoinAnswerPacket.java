package com.jftse.emulator.server.core.packet.packets.guild;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CGuildJoinAnswerPacket extends Packet {
    public S2CGuildJoinAnswerPacket(short result) {
        super(PacketOperations.S2CGuildJoinAnswer.getValueAsChar());

        this.write(result);
    }
}

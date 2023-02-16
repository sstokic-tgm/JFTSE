package com.jftse.emulator.server.core.packets.guild;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CGuildDeleteAnswerPacket extends Packet {
    public S2CGuildDeleteAnswerPacket(short result) {
        super(PacketOperations.S2CGuildDeleteAnswer);

        this.write(result);
    }
}
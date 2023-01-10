package com.jftse.emulator.server.core.packets.guild;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CGuildNameCheckAnswerPacket extends Packet {
    public S2CGuildNameCheckAnswerPacket(short result) {
        super(PacketOperations.S2CGuildNameCheckAnswer);

        this.write(result);
    }
}

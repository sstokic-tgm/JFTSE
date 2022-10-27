package com.jftse.emulator.server.core.packets.guild;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CGuildJoinAnswerPacket extends Packet {
    public S2CGuildJoinAnswerPacket(short result) {
        super(PacketOperations.S2CGuildJoinAnswer.getValue());

        this.write(result);
    }
}

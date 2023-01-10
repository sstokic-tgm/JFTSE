package com.jftse.emulator.server.core.packets.guild;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CGuildChangeReverseMemberAnswerPacket extends Packet {
    public S2CGuildChangeReverseMemberAnswerPacket(byte status, short result) {
        super(PacketOperations.S2CGuildChangeReverseMemberAnswer);

        this.write(status);
        this.write(result);
    }
}
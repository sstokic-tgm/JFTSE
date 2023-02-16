package com.jftse.emulator.server.core.packet.packets.guild;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CGuildChangeReverseMemberAnswerPacket extends Packet {
    public S2CGuildChangeReverseMemberAnswerPacket(byte status, short result) {
        super(PacketOperations.S2CGuildChangeReverseMemberAnswer.getValueAsChar());

        this.write(status);
        this.write(result);
    }
}
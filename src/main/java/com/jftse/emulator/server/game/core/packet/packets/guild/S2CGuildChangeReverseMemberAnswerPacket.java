package com.jftse.emulator.server.game.core.packet.packets.guild;

import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CGuildChangeReverseMemberAnswerPacket extends Packet {
    public S2CGuildChangeReverseMemberAnswerPacket(byte status, short result) {
        super(PacketID.S2CGuildChangeReverseMemberAnswer);

        this.write(status);
        this.write(result);
    }
}
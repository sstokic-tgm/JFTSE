package com.jftse.emulator.server.core.packet.packets.guild;

import com.jftse.emulator.server.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CGuildDeleteAnswerPacket extends Packet {
    public S2CGuildDeleteAnswerPacket(short result) {
        super(PacketID.S2CGuildDeleteAnswer);

        this.write(result);
    }
}
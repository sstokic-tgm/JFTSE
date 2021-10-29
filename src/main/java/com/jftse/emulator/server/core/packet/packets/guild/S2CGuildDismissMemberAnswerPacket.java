package com.jftse.emulator.server.core.packet.packets.guild;

import com.jftse.emulator.server.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CGuildDismissMemberAnswerPacket extends Packet {
    public S2CGuildDismissMemberAnswerPacket(short result) {
        super(PacketID.S2CGuildDismissMemberAnswer);

        this.write(result);
    }
}

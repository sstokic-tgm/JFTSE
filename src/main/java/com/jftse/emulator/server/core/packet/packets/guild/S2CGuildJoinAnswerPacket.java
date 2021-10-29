package com.jftse.emulator.server.core.packet.packets.guild;

import com.jftse.emulator.server.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CGuildJoinAnswerPacket extends Packet {
    public S2CGuildJoinAnswerPacket(short result) {
        super(PacketID.S2CGuildJoinAnswer);

        this.write(result);
    }
}

package com.jftse.emulator.server.game.core.packet.packets.guild;

import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CGuildNameCheckAnswerPacket extends Packet {
    public S2CGuildNameCheckAnswerPacket(short result) {
        super(PacketID.S2CGuildNameCheckAnswer);

        this.write(result);
    }
}

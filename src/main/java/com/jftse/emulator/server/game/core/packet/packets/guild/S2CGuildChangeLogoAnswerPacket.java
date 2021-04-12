package com.jftse.emulator.server.game.core.packet.packets.guild;

import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CGuildChangeLogoAnswerPacket extends Packet {
    public S2CGuildChangeLogoAnswerPacket(short result) {
        super(PacketID.S2CGuildChangeLogoAnswer);

        this.write(result);
    }
}
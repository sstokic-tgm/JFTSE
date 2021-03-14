package com.jftse.emulator.server.game.core.packet.packets.guild;

import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CGuildCreateAnswerPacket extends Packet {
    public S2CGuildCreateAnswerPacket(char result) {
        super(PacketID.S2CGuildCreateAnswer);

        this.write(result);
    }
}

package com.jftse.emulator.server.game.core.packet.packets.guild;

import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CGuildDismissMemberAnswerPacket extends Packet {
    public S2CGuildDismissMemberAnswerPacket(char result) {
        super(PacketID.S2CGuildDismissMemberAnswer);

        this.write(result);
    }
}

package com.jftse.emulator.server.game.core.packet.packets.guild;

import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CGuildChangeMasterAnswerPacket extends Packet {
    public S2CGuildChangeMasterAnswerPacket(short result) {
        super(PacketID.S2CGuildChangeMasterAnswer);

        this.write(result);
    }
}
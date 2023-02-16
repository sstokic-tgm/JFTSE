package com.jftse.emulator.server.core.packet.packets.guild;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CGuildChangeMasterAnswerPacket extends Packet {
    public S2CGuildChangeMasterAnswerPacket(short result) {
        super(PacketOperations.S2CGuildChangeMasterAnswer.getValueAsChar());

        this.write(result);
    }
}
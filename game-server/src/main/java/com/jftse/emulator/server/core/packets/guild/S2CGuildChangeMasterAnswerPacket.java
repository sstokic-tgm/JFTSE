package com.jftse.emulator.server.core.packets.guild;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CGuildChangeMasterAnswerPacket extends Packet {
    public S2CGuildChangeMasterAnswerPacket(short result) {
        super(PacketOperations.S2CGuildChangeMasterAnswer.getValue());

        this.write(result);
    }
}
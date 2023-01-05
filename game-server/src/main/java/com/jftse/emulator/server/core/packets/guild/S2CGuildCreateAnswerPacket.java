package com.jftse.emulator.server.core.packets.guild;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CGuildCreateAnswerPacket extends Packet {
    public S2CGuildCreateAnswerPacket(char result) {
        super(PacketOperations.S2CGuildCreateAnswer);

        this.write(result);
    }
}

package com.jftse.emulator.server.core.packets.pet;

import com.jftse.server.core.protocol.Packet;


public class S2CPetReviveAnswerPacket extends Packet {
    public S2CPetReviveAnswerPacket(short result) {
        super((char) 0x1); // temp until PacketOperations are updated
        // super(PacketOperations.S2CPetReviveAnswer.getValue());

        this.write(result);
    }
}

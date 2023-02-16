package com.jftse.emulator.server.core.packets.pet;

import com.jftse.server.core.protocol.Packet;


public class S2CPetNameChangeAnswerPacket extends Packet {
    public S2CPetNameChangeAnswerPacket(short result) {
        super((char) 0x1); // temp until PacketOperations are updated
        // super(PacketOperations.S2CPetNameChangeAnswer.getValue());

        this.write(result);
    }
}

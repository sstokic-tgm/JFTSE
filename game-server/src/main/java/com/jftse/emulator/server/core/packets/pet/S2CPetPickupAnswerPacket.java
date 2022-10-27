package com.jftse.emulator.server.core.packets.pet;

import com.jftse.server.core.protocol.Packet;


public class S2CPetPickupAnswerPacket extends Packet {
    public S2CPetPickupAnswerPacket(short result, Integer petType) {
        super((char) 0x1); // temp until PacketOperations are updated
        // super(PacketOperations.S2CPetPickupAnswer.getValue());

        this.write(result);
        this.write(petType);
    }
}

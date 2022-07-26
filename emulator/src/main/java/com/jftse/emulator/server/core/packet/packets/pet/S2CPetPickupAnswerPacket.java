package com.jftse.emulator.server.core.packet.packets.pet;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;


public class S2CPetPickupAnswerPacket extends Packet {
    public S2CPetPickupAnswerPacket(short result, Integer petType) {
        super((char) 0x1); // temp until PacketOperations are updated
        // super(PacketOperations.S2CPetPickupAnswer.getValueAsChar());

        this.write(result);
        this.write(petType);
    }
}

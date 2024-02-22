package com.jftse.emulator.server.core.packets.pet;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;


public class S2CPetPickupAnswerPacket extends Packet {
    public S2CPetPickupAnswerPacket(short result, Integer petType) {
        super(PacketOperations.S2CPetPickupAnswer);

        this.write(result);
        this.write(petType);
    }
}

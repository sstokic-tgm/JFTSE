package com.jftse.emulator.server.core.packet.packets.pet;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;


public class S2CPetNameChangeAnswerPacket extends Packet {
    public S2CPetNameChangeAnswerPacket(short result) {
        super(PacketOperations.S2CPetNameChangeAnswer.getValueAsChar());

        this.write(result);
    }
}

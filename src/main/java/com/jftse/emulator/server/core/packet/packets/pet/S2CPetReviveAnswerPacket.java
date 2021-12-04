package com.jftse.emulator.server.core.packet.packets.pet;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;


public class S2CPetReviveAnswerPacket extends Packet {
    public S2CPetReviveAnswerPacket(short result) {
        super(PacketOperations.S2CPetReviveAnswer.getValueAsChar());

        this.write(result);
    }
}

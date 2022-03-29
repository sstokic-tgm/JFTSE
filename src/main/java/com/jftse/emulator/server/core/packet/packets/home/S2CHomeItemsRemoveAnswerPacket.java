package com.jftse.emulator.server.core.packet.packets.home;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CHomeItemsRemoveAnswerPacket extends Packet {
    public S2CHomeItemsRemoveAnswerPacket(short result) {
        super(PacketOperations.S2CHomeItemsRemoveAnswer.getValueAsChar());

        this.write(result);
    }
}
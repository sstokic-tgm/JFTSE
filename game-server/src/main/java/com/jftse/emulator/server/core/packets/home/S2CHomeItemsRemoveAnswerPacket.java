package com.jftse.emulator.server.core.packets.home;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CHomeItemsRemoveAnswerPacket extends Packet {
    public S2CHomeItemsRemoveAnswerPacket(short result) {
        super(PacketOperations.S2CHomeItemsRemoveAnswer);

        this.write(result);
    }
}
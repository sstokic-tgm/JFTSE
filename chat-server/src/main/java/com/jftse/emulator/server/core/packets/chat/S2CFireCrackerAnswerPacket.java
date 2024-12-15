package com.jftse.emulator.server.core.packets.chat;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CFireCrackerAnswerPacket extends Packet {
    public S2CFireCrackerAnswerPacket(byte fireCrackerType, short position) {
        super(PacketOperations.S2CFireCrackerAnswer);

        this.write(fireCrackerType);
        this.write(position);
    }
}

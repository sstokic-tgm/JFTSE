package com.jftse.server.core.shared.packets;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CDisconnectAnswerPacket extends Packet {
    public S2CDisconnectAnswerPacket() {
        super(PacketOperations.S2CDisconnectAnswer.getValue());

        this.write((byte) 0);
    }
}

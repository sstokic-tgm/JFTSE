package com.jftse.emulator.server.core.packets.matchplay;

import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;

public class S2CMatchplayBackToRoom extends Packet {
    public S2CMatchplayBackToRoom() {
        super(PacketOperations.S2CMatchplayBackToRoom.getValue());

        this.write((char) 0);
    }
}
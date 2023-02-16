package com.jftse.emulator.server.core.packet.packets.matchplay;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CMatchplayBackToRoom extends Packet {
    public S2CMatchplayBackToRoom() {
        super(PacketOperations.S2CMatchplayBackToRoom.getValueAsChar());

        this.write((char) 0);
    }
}
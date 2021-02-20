package com.jftse.emulator.server.game.core.packet.packets.matchplay;

import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CMatchplayBackToRoom extends Packet {
    public S2CMatchplayBackToRoom() {
        super(PacketID.S2CMatchplayBackToRoom);

        this.write((char) 0);
    }
}
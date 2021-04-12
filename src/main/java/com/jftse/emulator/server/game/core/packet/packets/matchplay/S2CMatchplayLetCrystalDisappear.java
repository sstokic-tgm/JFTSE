package com.jftse.emulator.server.game.core.packet.packets.matchplay;

import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CMatchplayLetCrystalDisappear extends Packet {
    public S2CMatchplayLetCrystalDisappear(short skillIndex) {
        super(PacketID.S2CMatchplayLetCrystalDisappear);

        this.write(skillIndex);
    }
}
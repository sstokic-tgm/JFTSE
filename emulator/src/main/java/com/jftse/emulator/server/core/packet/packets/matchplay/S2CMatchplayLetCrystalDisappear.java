package com.jftse.emulator.server.core.packet.packets.matchplay;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CMatchplayLetCrystalDisappear extends Packet {
    public S2CMatchplayLetCrystalDisappear(short skillIndex) {
        super(PacketOperations.S2CMatchplayLetCrystalDisappear.getValueAsChar());

        this.write(skillIndex);
    }
}
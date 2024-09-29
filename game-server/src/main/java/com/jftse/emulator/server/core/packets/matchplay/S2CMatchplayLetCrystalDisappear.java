package com.jftse.emulator.server.core.packets.matchplay;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CMatchplayLetCrystalDisappear extends Packet {
    public S2CMatchplayLetCrystalDisappear(short skillIndex) {
        super(PacketOperations.S2CMatchplayLetCrystalDisappear);

        this.write(skillIndex);
    }
}
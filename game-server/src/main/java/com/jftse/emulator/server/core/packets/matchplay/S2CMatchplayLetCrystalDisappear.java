package com.jftse.emulator.server.core.packets.matchplay;

import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;

public class S2CMatchplayLetCrystalDisappear extends Packet {
    public S2CMatchplayLetCrystalDisappear(short skillIndex) {
        super(PacketOperations.S2CMatchplayLetCrystalDisappear.getValue());

        this.write(skillIndex);
    }
}
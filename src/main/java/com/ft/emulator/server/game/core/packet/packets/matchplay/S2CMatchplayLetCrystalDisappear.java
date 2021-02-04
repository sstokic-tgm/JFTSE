package com.ft.emulator.server.game.core.packet.packets.matchplay;

import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.networking.packet.Packet;

public class S2CMatchplayLetCrystalDisappear extends Packet {
    public S2CMatchplayLetCrystalDisappear(short skillIndex) {
        super(PacketID.S2CMatchplayLetCrystalDisappear);

        this.write(skillIndex);
    }
}
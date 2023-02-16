package com.jftse.emulator.server.core.packet.packets.matchplay;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CMatchplayGiveRandomSkill extends Packet {
    public S2CMatchplayGiveRandomSkill(short crystalId, byte position) {
        super(PacketOperations.S2CMatchplayGiveRandomSkill.getValueAsChar());

        this.write(crystalId);
        this.write(position);
    }
}
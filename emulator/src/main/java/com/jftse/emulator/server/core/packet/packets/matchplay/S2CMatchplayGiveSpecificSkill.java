package com.jftse.emulator.server.core.packet.packets.matchplay;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CMatchplayGiveSpecificSkill extends Packet {
    public S2CMatchplayGiveSpecificSkill(short crystalId, short position, int skillId) {
        super(PacketOperations.S2CMatchplayGiveSpecificSkill.getValueAsChar());

        this.write(crystalId);
        this.write(position);
        this.write(skillId);
    }
}
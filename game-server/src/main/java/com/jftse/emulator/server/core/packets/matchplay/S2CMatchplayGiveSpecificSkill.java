package com.jftse.emulator.server.core.packets.matchplay;

import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;

public class S2CMatchplayGiveSpecificSkill extends Packet {
    public S2CMatchplayGiveSpecificSkill(short crystalId, short position, int skillId) {
        super(PacketOperations.S2CMatchplayGiveSpecificSkill.getValue());

        this.write(crystalId);
        this.write(position);
        this.write(skillId);
    }
}
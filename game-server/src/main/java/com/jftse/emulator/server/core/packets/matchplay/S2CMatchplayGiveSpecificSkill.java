package com.jftse.emulator.server.core.packets.matchplay;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CMatchplayGiveSpecificSkill extends Packet {
    public S2CMatchplayGiveSpecificSkill(short crystalId, short position, int skillId) {
        super(PacketOperations.S2CMatchplayGiveSpecificSkill);

        this.write(crystalId);
        this.write(position);
        this.write(skillId);
    }
}
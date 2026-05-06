package com.jftse.emulator.server.core.packets.matchplay;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CMatchplayGivePlayerSkills extends Packet {
    public S2CMatchplayGivePlayerSkills(short playerPosition, int leftSkillIndex, int leftCrystalId, int rightSkillIndex, int rightCrystalId) {
        super(PacketOperations.S2CMatchplayGivePlayerSkills);

        this.write(playerPosition);
        this.write(leftSkillIndex);
        this.write(leftCrystalId);
        this.write(rightSkillIndex);
        this.write(rightCrystalId);
    }
}
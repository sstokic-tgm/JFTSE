package com.jftse.emulator.server.core.packets.matchplay;

import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;

public class S2CMatchplayGivePlayerSkills extends Packet {
    public S2CMatchplayGivePlayerSkills(short playerPosition, int leftSkillType, int rightSkillType) {
        super(PacketOperations.S2CMatchplayGivePlayerSkills);

        this.write(playerPosition);
        this.write(leftSkillType); // Skill type (left slot)
        this.write(0); // Unk
        this.write(rightSkillType); // SKill type (right slot)
        this.write(0); // Unk
    }
}
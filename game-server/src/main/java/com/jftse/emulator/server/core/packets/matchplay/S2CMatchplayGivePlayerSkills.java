package com.jftse.emulator.server.core.packets.matchplay;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CMatchplayGivePlayerSkills extends Packet {
    public S2CMatchplayGivePlayerSkills(short playerPosition, int leftSkillType, int leftCrystalId, int rightSkillType, int rightCrystalId) {
        super(PacketOperations.S2CMatchplayGivePlayerSkills);

        this.write(playerPosition);
        this.write(leftSkillType); // skillIndex (left slot)
        this.write(leftCrystalId); // left crystal id
        this.write(rightSkillType); // skillIndex (right slot)
        this.write(rightCrystalId); // right crystal id
    }
}
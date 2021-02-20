package com.jftse.emulator.server.game.core.packet.packets.matchplay;

import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CMatchplayGivePlayerSkills extends Packet {
    public S2CMatchplayGivePlayerSkills(short playerPosition, int leftSkillType, int rightSkillType) {
        super(PacketID.S2CMatchplayGivePlayerSkills);

        this.write(playerPosition);
        this.write(leftSkillType); // Skill type (left slot)
        this.write(0); // Unk
        this.write(rightSkillType); // SKill type (right slot)
        this.write(0); // Unk
    }
}
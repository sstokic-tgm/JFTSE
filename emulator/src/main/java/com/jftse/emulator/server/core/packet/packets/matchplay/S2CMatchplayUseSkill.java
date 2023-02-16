package com.jftse.emulator.server.core.packet.packets.matchplay;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CMatchplayUseSkill extends Packet {
    public S2CMatchplayUseSkill(byte attacker, byte target, byte skillId, byte seed, float xTarget, float zTarget, float yTarget) {
        super(PacketOperations.S2CMatchplayUseSkill.getValueAsChar());

        this.write(attacker);
        this.write(target);
        this.write(skillId);
        this.write(seed);
        this.write(xTarget);
        this.write(zTarget);
        this.write(yTarget);
    }
}
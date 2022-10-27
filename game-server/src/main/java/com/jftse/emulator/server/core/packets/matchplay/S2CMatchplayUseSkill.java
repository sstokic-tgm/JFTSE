package com.jftse.emulator.server.core.packets.matchplay;

import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;

public class S2CMatchplayUseSkill extends Packet {
    public S2CMatchplayUseSkill(byte attacker, byte target, byte skillId, byte seed, float xTarget, float zTarget, float yTarget) {
        super(PacketOperations.S2CMatchplayUseSkill.getValue());

        this.write(attacker);
        this.write(target);
        this.write(skillId);
        this.write(seed);
        this.write(xTarget);
        this.write(zTarget);
        this.write(yTarget);
    }
}
package com.jftse.emulator.server.core.packet.packets.matchplay;

import com.jftse.emulator.server.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CMatchplayUseSkill extends Packet {
    public S2CMatchplayUseSkill(byte attacker, byte target, byte skillId, byte seed, float xTarget, float zTarget, float yTarget) {
        super(PacketID.S2CMatchplayUseSkill);

        this.write(attacker);
        this.write(target);
        this.write(skillId);
        this.write(seed);
        this.write(xTarget);
        this.write(zTarget);
        this.write(yTarget);
    }
}
package com.jftse.emulator.server.game.core.packet.packets.matchplay;

import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

import java.awt.geom.Point2D;

public class S2CMatchplayUseSkill extends Packet {
    public S2CMatchplayUseSkill(byte attacker, byte target, byte skillId, Point2D point) {
        super(PacketID.S2CMatchplayUseSkill);

        this.write(attacker); // ATTACKING ENTITY
        this.write(target); // TARGET ENTITY
        this.write(skillId); // Skill id
        this.write((byte)  0); // ??
        this.write((float) point.getX()); // X
        this.write((float) 0); // Y
        this.write((float) point.getY()); // Z
    }
}
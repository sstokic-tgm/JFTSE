package com.jftse.emulator.server.core.packet.packets.matchplay;

import com.jftse.emulator.server.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

import java.awt.geom.Point2D;

public class S2CMatchplayPlaceSkillCrystal extends Packet {
    public S2CMatchplayPlaceSkillCrystal(short skillIndex, Point2D point) {
        super(PacketID.S2CMatchplayPlaceSkillCrystal);

        this.write(skillIndex); // Skill index we want to set
        this.write((byte) 0); // Unk
        this.write((float) point.getX()); // X-Position (same dimension as in serve)
        this.write((float) point.getY()); // Y-Position (same dimension as in serve)
    }
}
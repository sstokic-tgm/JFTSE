package com.ft.emulator.server.game.core.packet.packets.matchplay;

import com.ft.emulator.server.game.core.matchplay.room.ServeInfo;
import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.networking.packet.Packet;

import java.util.List;

public class S2CMatchplayPlaceSkillCrystal extends Packet {
    public S2CMatchplayPlaceSkillCrystal(short skillIndex, float xPosition, float yPosition) {
        super(PacketID.S2CMatchplayPlaceSkillCrystal);

        this.write(skillIndex); // Skill index we want to set
        this.write((byte) 0); // Unk
        this.write(xPosition); // X-Position (same dimension as in serve)
        this.write(yPosition); // Y-Position (same dimension as in serve)
    }
}
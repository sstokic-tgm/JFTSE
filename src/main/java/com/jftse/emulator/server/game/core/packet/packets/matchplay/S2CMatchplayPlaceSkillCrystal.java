package com.jftse.emulator.server.game.core.packet.packets.matchplay;

import com.jftse.emulator.server.game.core.matchplay.room.ServeInfo;
import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;

public class S2CMatchplayPlaceSkillCrystal extends Packet {
    public S2CMatchplayPlaceSkillCrystal(List<ServeInfo> serveInfo) {
        super(PacketID.S2CMatchplayPlaceSkillCrystal);

        this.write((short) 0); // Skill index we want to set
        this.write((byte) 0); // Unk
        this.write((float) -20); // X-Position (same dimension as in serve)
        this.write((float) -60); // Y-Position (same dimension as in serve)
    }
}
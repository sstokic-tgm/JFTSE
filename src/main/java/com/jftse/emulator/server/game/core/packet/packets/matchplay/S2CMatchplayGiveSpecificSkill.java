package com.jftse.emulator.server.game.core.packet.packets.matchplay;

import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CMatchplayGiveSpecificSkill extends Packet {
    public S2CMatchplayGiveSpecificSkill(short crystalId, short position, int skillId) {
        super(PacketID.S2CMatchplayGiveSpecificSkill);

        this.write(crystalId);
        this.write(position);
        this.write(skillId);
    }
}
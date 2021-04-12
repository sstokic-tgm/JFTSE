package com.jftse.emulator.server.game.core.packet.packets.matchplay;

import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CMatchplayGiveRandomSkill extends Packet {
    public S2CMatchplayGiveRandomSkill(short crystalId, byte position) {
        super(PacketID.S2CMatchplayGiveRandomSkill);

        this.write(crystalId);
        this.write(position);
    }
}
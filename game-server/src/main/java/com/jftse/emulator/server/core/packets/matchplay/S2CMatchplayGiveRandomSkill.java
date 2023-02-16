package com.jftse.emulator.server.core.packets.matchplay;

import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;

public class S2CMatchplayGiveRandomSkill extends Packet {
    public S2CMatchplayGiveRandomSkill(short crystalId, byte position) {
        super(PacketOperations.S2CMatchplayGiveRandomSkill);

        this.write(crystalId);
        this.write(position);
    }
}
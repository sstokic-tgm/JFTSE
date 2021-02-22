package com.jftse.emulator.server.game.core.packet.packets.matchplay;

import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CMatchplayTriggerRandomGuardianSkill extends Packet {
    public S2CMatchplayTriggerRandomGuardianSkill(byte guardianPosition) {
        super(PacketID.S2CMatchplayTriggerGuardianCastRandomSkill);

        this.write((short) 0);
        this.write(guardianPosition);
    }
}
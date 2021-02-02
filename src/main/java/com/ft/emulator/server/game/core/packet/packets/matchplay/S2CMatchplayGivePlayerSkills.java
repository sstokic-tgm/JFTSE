package com.ft.emulator.server.game.core.packet.packets.matchplay;

import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.networking.packet.Packet;

public class S2CMatchplayGivePlayerSkills extends Packet {
    public S2CMatchplayGivePlayerSkills() {
        super(PacketID.S2CMatchplayGivePlayerSkills);

        this.write((short) 0);
        this.write(0); // Skill type (left slot)
        this.write(0); // Unk
        this.write(0); // SKill type (right slot)
        this.write(0); // Unk
    }
}
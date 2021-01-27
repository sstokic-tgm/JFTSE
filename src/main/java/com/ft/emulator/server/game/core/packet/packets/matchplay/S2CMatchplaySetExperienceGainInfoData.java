package com.ft.emulator.server.game.core.packet.packets.matchplay;

import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.networking.packet.Packet;

public class S2CMatchplaySetExperienceGainInfoData extends Packet {
    public S2CMatchplaySetExperienceGainInfoData(byte resultTitle, int secondsNeeded) {
        super(PacketID.S2CMatchPlaySetExperienceGainInfoData);

        this.write(resultTitle); // 0 = Loser, 1 = Winner
        this.write((byte) 20); // level

        this.write(10); // EXP BASIC
        this.write(20); // GOLD BASIC
        this.write(30); // EXP BONUS
        this.write(40); // GOLD BONUS
        this.write(50); // EXP TOTAL -> current exp + won exp
        this.write(60); // GOLD TOTAL -> current gold + won gold

        this.write((byte) 20); // Unk
        this.write((byte) 30); // Unk

        this.write(secondsNeeded); // Playtime in seconds
        this.write(10); // Ranking point reward
        this.write(0); // Unk
        this.write(0); // Unk
        this.write(1); // Bonus

        this.write((byte) 0); // Unk
        this.write(0); // Unk
        this.write(0); // Unk
        this.write(0); // Unk
    }
}
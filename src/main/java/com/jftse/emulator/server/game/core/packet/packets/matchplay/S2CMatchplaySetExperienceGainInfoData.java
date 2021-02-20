package com.jftse.emulator.server.game.core.packet.packets.matchplay;

import com.jftse.emulator.server.game.core.matchplay.PlayerReward;
import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CMatchplaySetExperienceGainInfoData extends Packet {
    public S2CMatchplaySetExperienceGainInfoData(byte resultTitle, int secondsNeeded, PlayerReward playerReward, byte level) {
        super(PacketID.S2CMatchPlaySetExperienceGainInfoData);

        this.write(resultTitle); // 0 = Loser, 1 = Winner
        this.write(level); // level

        this.write(playerReward.getBasicRewardExp()); // EXP BASIC
        this.write(playerReward.getBasicRewardGold()); // GOLD BASIC
        this.write(0); // EXP BONUS
        this.write(0); // GOLD BONUS
        this.write(playerReward.getBasicRewardExp()); // EXP TOTAL -> current exp + won exp
        this.write(playerReward.getBasicRewardGold()); // GOLD TOTAL -> current gold + won gold

        this.write((byte) 0); // perfects
        this.write((byte) 0); // guards

        this.write(secondsNeeded); // Playtime in seconds
        this.write(0); // Ranking point reward
        this.write(0); // Unk
        this.write(0); // Unk
        this.write(0); // Bonus (1 = Perfect, ...)

        this.write((byte) 0); // Unk
        this.write(0); // Unk
        this.write(0); // Unk
        this.write(0); // Unk
    }
}
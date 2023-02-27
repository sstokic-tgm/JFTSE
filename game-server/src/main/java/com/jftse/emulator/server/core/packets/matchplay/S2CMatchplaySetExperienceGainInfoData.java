package com.jftse.emulator.server.core.packets.matchplay;

import com.jftse.emulator.server.core.matchplay.PlayerReward;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;

import java.util.ArrayList;

public class S2CMatchplaySetExperienceGainInfoData extends Packet {
    public S2CMatchplaySetExperienceGainInfoData(byte resultTitle, int secondsNeeded, PlayerReward playerReward, byte level, ArrayList<Short> bonusResultGameData) {
        super(PacketOperations.S2CMatchPlaySetExperienceGainInfoData);

        this.write(resultTitle); // 0 = Loser, 1 = Winner
        this.write(level); // level

        this.write(playerReward != null ? playerReward.getRewardExp() : 0); // EXP BASIC
        this.write(playerReward != null ? playerReward.getRewardGold() : 0); // GOLD BASIC
        this.write(0); // EXP BONUS
        this.write(0); // GOLD BONUS
        this.write(playerReward != null ? playerReward.getRewardExp() : 0); // EXP TOTAL -> current exp + won exp
        this.write(playerReward != null ? playerReward.getRewardGold() : 0); // GOLD TOTAL -> current gold + won gold

        this.write((byte) 0); // perfects
        this.write((byte) 0); // guards

        this.write(secondsNeeded); // Playtime in seconds
        this.write(playerReward != null ? playerReward.getRewardRP() : 0); // Ranking point reward
        this.write(0); // Unk
        this.write(0); // Unk

        /******* Bonus *******/
        this.write(bonusResultGameData.get(0).byteValue()); // 0000 0001 = PF, 0000 0010 = GB, 0000 0100 = Time, 0000 1000 = matchplay, 0001 0000 = Lv up, ...
        this.write(bonusResultGameData.get(1).byteValue()); // 0000 0001 = Couple Bonus
        this.write(bonusResultGameData.get(2).byteValue()); // 0000 0001 = EXP Bonus, 0000 0010 = Gold Bonus, 0000 1000 = Ring Wiseman, 0000 0100 = Event
    }
}
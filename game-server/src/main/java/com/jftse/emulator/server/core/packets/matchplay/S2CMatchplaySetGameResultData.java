package com.jftse.emulator.server.core.packets.matchplay;

import com.jftse.emulator.server.core.matchplay.PlayerReward;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;

import java.util.List;

public class S2CMatchplaySetGameResultData extends Packet {
    public S2CMatchplaySetGameResultData(List<PlayerReward> playerRewards, List<Short> bonusResultGameData) {
        super(PacketOperations.S2CMatchplaySetGameResultData);

        this.write((byte) playerRewards.size());
        for (PlayerReward playerReward : playerRewards) {
            this.write(playerReward.getPlayerPosition());
            this.write(playerReward.getRewardExp()); // EXP
            this.write(playerReward.getRewardGold()); // GOLD
            /******* Bonus *******/
            //this.write((byte) 0); // 0000 0001 = PF, 0000 0010 = GB, 0000 0100 = Time, 0000 1000 = matchplay, 0001 0000 = Lv up, ...
            //this.write((byte) 1); // 0000 0001 = Couple Bonus
            //this.write((byte) 31); // 0000 0001 = EXP Bonus, 0000 0010 = Gold Bonus, 0000 1000 = Ring Wiseman, 0000 0100 = Event

            // TODO: the structure of packet is here an int, int is 4 bytes long, which means here is missing a byte, at the start or at the end, since it's 3 bytes now
            // also bonusResultGameData should/must be inside playerReward in this case
            this.write(0);
            // this.write(bonusResultGameData.get(0).byteValue()); // 0000 0001 = PF, 0000 0010 = GB, 0000 0100 = Time, 0000 1000 = matchplay, 0001 0000 = Lv up, ...
            // this.write(bonusResultGameData.get(1).byteValue()); // 0000 0001 = Couple Bonus
            // this.write(bonusResultGameData.get(2).byteValue()); // 0000 0001 = EXP Bonus, 0000 0010 = Gold Bonus, 0000 1000 = Ring Wiseman, 0000 0100 = Event
        }
    }
}
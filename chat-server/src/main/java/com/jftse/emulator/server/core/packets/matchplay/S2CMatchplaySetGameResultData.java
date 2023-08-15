package com.jftse.emulator.server.core.packets.matchplay;

import com.jftse.emulator.server.core.matchplay.PlayerReward;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;

import java.util.List;

public class S2CMatchplaySetGameResultData extends Packet {
    public S2CMatchplaySetGameResultData(List<PlayerReward> playerRewards) {
        super(PacketOperations.S2CMatchplaySetGameResultData);

        this.write((byte) playerRewards.size());
        for (PlayerReward playerReward : playerRewards) {
            this.write(playerReward.getPlayerPosition());
            this.write(playerReward.getExp()); // EXP
            this.write(playerReward.getGold()); // GOLD

            // 0000 0001 = PF, 0000 0010 = GB, 0000 0100 = Time, 0000 1000 = matchplay, 0001 0000 = Lv up, ...
            // 0000 0001 = Couple Bonus
            // 0000 0001 = EXP Bonus, 0000 0010 = Gold Bonus, 0000 1000 = Ring Wiseman, 0000 0100 = Event
            this.write(playerReward.getActiveBonuses());
        }
    }
}
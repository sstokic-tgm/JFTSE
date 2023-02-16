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
            this.write(playerReward.getRewardExp()); // EXP
            this.write(playerReward.getRewardGold()); // GOLD
            this.write(0); // Bonuses
        }
    }
}
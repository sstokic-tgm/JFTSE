package com.jftse.emulator.server.game.core.packet.packets.matchplay;

import com.jftse.emulator.server.game.core.matchplay.PlayerReward;
import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;

public class S2CMatchplaySetGameResultData extends Packet {
    public S2CMatchplaySetGameResultData(List<PlayerReward> playerRewards) {
        super(PacketID.S2CMatchplaySetGameResultData);

        this.write((byte) playerRewards.size());
        for (PlayerReward playerReward : playerRewards) {
            this.write(playerReward.getPlayerPosition());
            this.write(playerReward.getBasicRewardExp()); // EXP
            this.write(playerReward.getBasicRewardGold()); // GOLD
            this.write(0); // Bonuses
        }
    }
}
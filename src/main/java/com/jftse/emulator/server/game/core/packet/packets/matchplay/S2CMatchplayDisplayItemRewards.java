package com.jftse.emulator.server.game.core.packet.packets.matchplay;

import com.jftse.emulator.server.game.core.matchplay.PlayerReward;
import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;

public class S2CMatchplayDisplayItemRewards extends Packet {
    public S2CMatchplayDisplayItemRewards(List<PlayerReward> playerRewardList) {
        super(PacketID.S2CMatchplayDisplayItemRewards);

        this.write((byte) 0); //Unk
        this.write((byte) 0); //Unk
        this.write(0); //Unk

        this.write((byte) 4);
        for (int i = 0; i < 4; i++) {
            final int index = i;
            PlayerReward playerReward = playerRewardList.stream().filter(x -> x.getPlayerPosition() == index)
                    .findFirst()
                    .orElse(null);
            if (playerReward == null) {
                this.write(0);
                this.write(0);
            } else {
                this.write(playerReward.getRewardProductIndex());
                this.write(playerReward.getProductRewardAmount());
            }
        }
    }
}
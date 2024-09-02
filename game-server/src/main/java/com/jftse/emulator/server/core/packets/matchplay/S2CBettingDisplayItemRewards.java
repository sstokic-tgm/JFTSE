package com.jftse.emulator.server.core.packets.matchplay;

import com.jftse.emulator.server.core.matchplay.PlayerReward;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;

import java.util.List;

public class S2CBettingDisplayItemRewards extends Packet {
    public S2CBettingDisplayItemRewards(List<PlayerReward> playerRewardList) {
        super(PacketOperations.S2CBettingDisplayItemRewards);

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
                this.write(playerReward.getProductIndex());
                this.write(playerReward.getProductAmount());
            }
        }
    }
}
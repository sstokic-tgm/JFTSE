package com.jftse.emulator.server.core.packets.matchplay;

import com.jftse.emulator.server.core.matchplay.MatchplayReward;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

import java.util.List;

public class S2CMatchplayItemRewardsPacket extends Packet {
    public S2CMatchplayItemRewardsPacket(MatchplayReward matchplayReward) {
        super(PacketOperations.S2CMatchplayItemRewards);

        this.write((byte) 0);

        List<MatchplayReward.ItemReward> itemRewards = matchplayReward.getItemRewards();
        for (MatchplayReward.ItemReward itemReward : itemRewards) {
            this.write((byte) 0); // 0 = product, 1 = material
            this.write(itemReward.getProductIndex());
            this.write(itemReward.getProductAmount());
        }
    }
}

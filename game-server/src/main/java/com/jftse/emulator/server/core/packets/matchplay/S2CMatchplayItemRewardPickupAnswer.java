package com.jftse.emulator.server.core.packets.matchplay;

import com.jftse.emulator.server.core.matchplay.MatchplayReward;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CMatchplayItemRewardPickupAnswer extends Packet {
    public S2CMatchplayItemRewardPickupAnswer(byte playerPos, byte slot, MatchplayReward.ItemReward itemReward) {
        super(PacketOperations.S2CMatchplayItemRewardPickupAnswer);

        this.write(playerPos);
        this.write(slot);
        this.write((byte) 0); // 0 = product, 1 = material
        this.write(itemReward.getProductIndex());
        this.write(itemReward.getProductAmount());
    }
}

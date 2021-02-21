package com.jftse.emulator.server.game.core.packet.packets.matchplay;

import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CMatchplayDisplayItemRewards extends Packet {
    public S2CMatchplayDisplayItemRewards(byte itemRewardsCount) {
        super(PacketID.S2CMatchplayDisplayItemRewards);

        this.write((byte) 0); //Unk
        this.write((byte) 0); //Unk
        this.write(0); //Unk

        this.write(itemRewardsCount);
        for (int i = 0; i < itemRewardsCount; i++) {
            this.write(0); // ProductIndex
            this.write(1); // Quantity
        }
    }
}
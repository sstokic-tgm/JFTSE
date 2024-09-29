package com.jftse.emulator.server.core.packets.inventory;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CInventorySellItemAnswerPacket extends Packet {
    public S2CInventorySellItemAnswerPacket(char itemCount, int itemPocketId) {
        super(PacketOperations.S2CInventorySellItemAnswer);

        this.write(itemCount);

        for (char i = 0; i < itemCount; ++i)
            this.write(itemPocketId);
    }
}

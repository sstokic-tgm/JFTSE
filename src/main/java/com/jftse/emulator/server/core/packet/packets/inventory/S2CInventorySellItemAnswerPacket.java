package com.jftse.emulator.server.core.packet.packets.inventory;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CInventorySellItemAnswerPacket extends Packet {
    public S2CInventorySellItemAnswerPacket(char itemCount, int itemPocketId) {
        super(PacketOperations.S2CInventorySellItemAnswer.getValueAsChar());

        this.write(itemCount);

        for (char i = 0; i < itemCount; ++i)
            this.write(itemPocketId);
    }
}

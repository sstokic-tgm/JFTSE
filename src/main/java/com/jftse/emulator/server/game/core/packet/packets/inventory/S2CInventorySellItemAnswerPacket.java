package com.jftse.emulator.server.game.core.packet.packets.inventory;

import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CInventorySellItemAnswerPacket extends Packet {
    public S2CInventorySellItemAnswerPacket(char itemCount, int itemPocketId) {
        super(PacketID.S2CInventorySellItemAnswer);

        this.write(itemCount);

        for (char i = 0; i < itemCount; ++i)
            this.write(itemPocketId);
    }
}

package com.ft.emulator.server.game.server.packets.inventory;

import com.ft.emulator.server.game.server.Packet;
import com.ft.emulator.server.game.server.PacketID;

public class S2CInventorySellItemAnserPacket extends Packet {

    public S2CInventorySellItemAnserPacket(char itemCount, int itemPocketId) {

        super(PacketID.S2CInventorySellItemAnswer);

        this.write(itemCount);

        for(char i = 0; i < itemCount; i++) {
            this.write(itemPocketId);
	}
    }
}
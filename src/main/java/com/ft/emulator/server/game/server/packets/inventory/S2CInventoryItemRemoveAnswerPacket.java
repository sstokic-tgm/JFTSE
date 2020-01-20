package com.ft.emulator.server.game.server.packets.inventory;

import com.ft.emulator.server.game.server.Packet;
import com.ft.emulator.server.game.server.PacketID;

public class S2CInventoryItemRemoveAnswerPacket extends Packet {

    public S2CInventoryItemRemoveAnswerPacket(int itemPocketId) {

        super(PacketID.S2CInventoryItemRemoveAnswer);

        this.write(itemPocketId);
    }
}
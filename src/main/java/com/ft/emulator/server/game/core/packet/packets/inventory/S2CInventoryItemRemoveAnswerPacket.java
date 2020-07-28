package com.ft.emulator.server.game.core.packet.packets.inventory;

import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.networking.packet.Packet;

public class S2CInventoryItemRemoveAnswerPacket extends Packet {
    public S2CInventoryItemRemoveAnswerPacket(int itemPocketId) {
        super(PacketID.S2CInventoryItemRemoveAnswer);

        this.write(itemPocketId);
    }
}

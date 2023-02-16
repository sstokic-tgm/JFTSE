package com.jftse.emulator.server.core.packet.packets.inventory;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CInventoryItemRemoveAnswerPacket extends Packet {
    public S2CInventoryItemRemoveAnswerPacket(int itemPocketId) {
        super(PacketOperations.S2CInventoryItemRemoveAnswer.getValueAsChar());

        this.write(itemPocketId);
    }
}

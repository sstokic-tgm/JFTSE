package com.jftse.server.core.shared.packets.inventory;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CInventoryItemRemoveAnswerPacket extends Packet {
    public S2CInventoryItemRemoveAnswerPacket(int itemPocketId) {
        super(PacketOperations.S2CInventoryItemRemoveAnswer);

        this.write(itemPocketId);
    }
}

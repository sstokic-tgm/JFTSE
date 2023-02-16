package com.jftse.emulator.server.core.packets.inventory;

import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;

public class S2CInventoryExpandAnswerPacket extends Packet {

    // -1 MSG_INCREASE_INVENTORY_COUNT_FAILED_01
    // 0 MSG_INCREASE_INVENTORY_COUNT_SUCCESS
    public S2CInventoryExpandAnswerPacket(byte status, short pocketSize) {
        super(PacketOperations.S2CInventoryExpandAnswer);

        this.write(status);
        this.write(pocketSize);
    }
}

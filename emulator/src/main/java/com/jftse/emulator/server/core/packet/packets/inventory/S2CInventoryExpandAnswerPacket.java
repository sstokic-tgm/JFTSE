package com.jftse.emulator.server.core.packet.packets.inventory;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CInventoryExpandAnswerPacket extends Packet {

    // -1 MSG_INCREASE_INVENTORY_COUNT_FAILED_01
    // 0 MSG_INCREASE_INVENTORY_COUNT_SUCCESS
    public S2CInventoryExpandAnswerPacket(byte status, short pocketSize) {
        super(PacketOperations.S2CInventoryExpandAnswer.getValueAsChar());

        this.write(status);
        this.write(pocketSize);
    }
}

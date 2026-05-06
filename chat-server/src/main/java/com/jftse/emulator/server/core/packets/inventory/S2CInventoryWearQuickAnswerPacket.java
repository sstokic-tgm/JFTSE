package com.jftse.emulator.server.core.packets.inventory;

import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;

import java.util.List;

public class S2CInventoryWearQuickAnswerPacket extends Packet {
    public S2CInventoryWearQuickAnswerPacket(List<Integer> quickSlotList) {
        super(PacketOperations.S2CInventoryWearQuickAnswer);

        quickSlotList.forEach(this::write);
    }
}

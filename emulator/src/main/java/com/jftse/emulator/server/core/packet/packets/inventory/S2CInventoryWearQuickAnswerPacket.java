package com.jftse.emulator.server.core.packet.packets.inventory;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;

public class S2CInventoryWearQuickAnswerPacket extends Packet {
    public S2CInventoryWearQuickAnswerPacket(List<Integer> quickSlotList) {
        super(PacketOperations.S2CInventoryWearQuickAnswer.getValueAsChar());

        quickSlotList.forEach(this::write);
    }
}

package com.jftse.emulator.server.core.packet.packets.inventory;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;

public class S2CInventoryWearToolAnswerPacket extends Packet {
    public S2CInventoryWearToolAnswerPacket(List<Integer> quickSlotList) {
        super(PacketOperations.S2CInventoryWearToolAnswer.getValueAsChar());

        quickSlotList.forEach(this::write);
    }
}
package com.jftse.emulator.server.core.packet.packets.inventory;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;

public class S2CInventoryWearSpecialAnswerPacket extends Packet {
    public S2CInventoryWearSpecialAnswerPacket(List<Integer> specialSlotList) {
        super(PacketOperations.S2CInventoryWearSpecialAnswer.getValueAsChar());

        specialSlotList.forEach(this::write);
    }
}
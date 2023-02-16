package com.jftse.emulator.server.core.packets.inventory;

import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;

import java.util.List;

public class S2CInventoryWearSpecialAnswerPacket extends Packet {
    public S2CInventoryWearSpecialAnswerPacket(List<Integer> specialSlotList) {
        super(PacketOperations.S2CInventoryWearSpecialAnswer);

        specialSlotList.forEach(this::write);
    }
}
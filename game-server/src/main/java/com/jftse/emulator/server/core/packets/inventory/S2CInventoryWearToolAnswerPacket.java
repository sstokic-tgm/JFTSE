package com.jftse.emulator.server.core.packets.inventory;

import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;

import java.util.List;

public class S2CInventoryWearToolAnswerPacket extends Packet {
    public S2CInventoryWearToolAnswerPacket(List<Integer> toolSlotList) {
        super(PacketOperations.S2CInventoryWearToolAnswer.getValue());

        toolSlotList.forEach(this::write);
    }
}
package com.jftse.emulator.server.core.packets.inventory;

import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;

import java.util.List;

public class S2CInventoryWearCardAnswerPacket extends Packet {
    public S2CInventoryWearCardAnswerPacket(List<Integer> cardSlotList) {
        super(PacketOperations.S2CInventoryWearCardAnswer);

        cardSlotList.forEach(this::write);
    }
}
package com.jftse.emulator.server.core.packet.packets.inventory;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;

public class S2CInventoryWearCardAnswerPacket extends Packet {
    public S2CInventoryWearCardAnswerPacket(List<Integer> cardSlotList) {
        super(PacketOperations.S2CInventoryWearCardAnswer.getValueAsChar());

        cardSlotList.forEach(this::write);
    }
}
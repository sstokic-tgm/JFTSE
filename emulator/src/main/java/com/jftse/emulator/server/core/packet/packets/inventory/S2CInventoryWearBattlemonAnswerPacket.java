package com.jftse.emulator.server.core.packet.packets.inventory;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;

public class S2CInventoryWearBattlemonAnswerPacket extends Packet {
    public S2CInventoryWearBattlemonAnswerPacket(List<Integer> battlemonSlotList) {
        super(PacketOperations.S2CInventoryWearBattlemonAnswer.getValueAsChar());

        battlemonSlotList.forEach(this::write);
    }
}
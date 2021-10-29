package com.jftse.emulator.server.core.packet.packets.inventory;

import com.jftse.emulator.server.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;

public class S2CInventoryWearToolAnswerPacket extends Packet {
    public S2CInventoryWearToolAnswerPacket(List<Integer> quickSlotList) {
        super(PacketID.S2CInventoryWearToolAnswer);

        quickSlotList.forEach(this::write);
    }
}
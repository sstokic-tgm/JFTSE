package com.jftse.emulator.server.core.packet.packets.inventory;

import com.jftse.emulator.server.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;

public class S2CInventoryWearCardAnswerPacket extends Packet {
    public S2CInventoryWearCardAnswerPacket(List<Integer> cardSlotList) {
        super(PacketID.S2CInventoryWearCardAnswer);

        cardSlotList.forEach(this::write);
    }
}
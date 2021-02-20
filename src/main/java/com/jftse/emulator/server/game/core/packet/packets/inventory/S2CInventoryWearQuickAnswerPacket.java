package com.jftse.emulator.server.game.core.packet.packets.inventory;

import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;

public class S2CInventoryWearQuickAnswerPacket extends Packet {
    public S2CInventoryWearQuickAnswerPacket(List<Integer> quickSlotList) {
        super(PacketID.S2CInventoryWearQuickAnswer);

        quickSlotList.forEach(this::write);
    }
}

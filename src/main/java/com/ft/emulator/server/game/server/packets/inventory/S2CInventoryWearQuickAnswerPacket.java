package com.ft.emulator.server.game.server.packets.inventory;

import com.ft.emulator.server.game.server.Packet;
import com.ft.emulator.server.game.server.PacketID;

import java.util.List;

public class S2CInventoryWearQuickAnswerPacket extends Packet {

    public S2CInventoryWearQuickAnswerPacket(List<Integer> quickSlotList) {

        super(PacketID.S2CInventoryWearQuickAnswer);

        quickSlotList.forEach(this::write);
    }
}
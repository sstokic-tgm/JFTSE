package com.jftse.emulator.server.game.core.packet.packets.inventory;

import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;

public class S2CInventoryWearSpecialAnswerPacket extends Packet {
    public S2CInventoryWearSpecialAnswerPacket(List<Integer> specialSlotList) {
        super(PacketID.S2CInventoryWearSpecialAnswer);

        specialSlotList.forEach(this::write);
    }
}
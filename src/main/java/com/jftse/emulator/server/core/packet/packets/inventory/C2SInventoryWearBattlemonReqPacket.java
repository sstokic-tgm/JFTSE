package com.jftse.emulator.server.core.packet.packets.inventory;

import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class C2SInventoryWearBattlemonReqPacket extends Packet {
    private List<Integer> battlemonSlotList;

    public C2SInventoryWearBattlemonReqPacket(Packet packet) {
        super(packet);

        battlemonSlotList = new ArrayList<>();

        for (int i = 0; i < 2; ++i)
            battlemonSlotList.add(this.readInt());
    }
}
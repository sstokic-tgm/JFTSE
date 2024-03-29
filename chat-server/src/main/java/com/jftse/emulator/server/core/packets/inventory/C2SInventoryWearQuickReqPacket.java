package com.jftse.emulator.server.core.packets.inventory;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class C2SInventoryWearQuickReqPacket extends Packet {
    private List<Integer> quickSlotList;

    public C2SInventoryWearQuickReqPacket(Packet packet) {
        super(packet);

        quickSlotList = new ArrayList<>();

        for (int i = 0; i < 5; ++i)
            quickSlotList.add(this.readInt());
    }
}

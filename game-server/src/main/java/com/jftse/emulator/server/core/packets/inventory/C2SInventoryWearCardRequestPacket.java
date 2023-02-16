package com.jftse.emulator.server.core.packets.inventory;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class C2SInventoryWearCardRequestPacket extends Packet {
    private List<Integer> cardSlotList;

    public C2SInventoryWearCardRequestPacket(Packet packet) {
        super(packet);

        cardSlotList = new ArrayList<>();

        for (int i = 0; i < 4; ++i)
            cardSlotList.add(this.readInt());
    }
}

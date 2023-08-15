package com.jftse.emulator.server.core.packets.inventory;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SInventoryItemTimeExpiredReqPacket extends Packet {
    private int itemPocketId;

    public C2SInventoryItemTimeExpiredReqPacket(Packet packet) {
        super(packet);

        this.itemPocketId = this.readInt();
    }
}

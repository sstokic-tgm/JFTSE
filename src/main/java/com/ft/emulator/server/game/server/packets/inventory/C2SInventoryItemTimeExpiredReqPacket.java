package com.ft.emulator.server.game.server.packets.inventory;

import com.ft.emulator.server.game.server.Packet;
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
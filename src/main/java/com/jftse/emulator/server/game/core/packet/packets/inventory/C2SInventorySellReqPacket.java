package com.jftse.emulator.server.game.core.packet.packets.inventory;

import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SInventorySellReqPacket extends Packet {
    private int itemPocketId;

    public C2SInventorySellReqPacket(Packet packet) {
        super(packet);

        this.itemPocketId = this.readInt();
    }
}

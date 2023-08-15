package com.jftse.emulator.server.core.packets.home;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SHomeItemsRemoveReqPacket extends Packet {
    private int homeInventoryId;

    public C2SHomeItemsRemoveReqPacket(Packet packet) {
        super(packet);

        this.homeInventoryId = this.readInt();
    }
}
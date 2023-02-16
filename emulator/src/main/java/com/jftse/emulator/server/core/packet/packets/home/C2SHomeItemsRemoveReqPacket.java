package com.jftse.emulator.server.core.packet.packets.home;

import com.jftse.emulator.server.networking.packet.Packet;
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
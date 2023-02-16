package com.jftse.emulator.server.core.packets.player;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SQuickSlotUseRequestPacket extends Packet {
    private int quickSlotId;

    public C2SQuickSlotUseRequestPacket(Packet packet) {
        super(packet);
        this.quickSlotId = this.readInt();
    }
}

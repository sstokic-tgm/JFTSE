package com.jftse.emulator.server.core.packet.packets.player;

import com.jftse.emulator.server.networking.packet.Packet;
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

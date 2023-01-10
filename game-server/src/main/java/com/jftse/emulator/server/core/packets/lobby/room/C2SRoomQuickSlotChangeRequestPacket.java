package com.jftse.emulator.server.core.packets.lobby.room;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SRoomQuickSlotChangeRequestPacket extends Packet {
    private boolean quickSlot;

    public C2SRoomQuickSlotChangeRequestPacket(Packet packet) {
        super(packet);

        this.quickSlot = this.readByte() == 1;
    }
}
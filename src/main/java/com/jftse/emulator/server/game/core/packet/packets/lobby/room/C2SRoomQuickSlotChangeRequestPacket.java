package com.jftse.emulator.server.game.core.packet.packets.lobby.room;

import com.jftse.emulator.server.networking.packet.Packet;
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
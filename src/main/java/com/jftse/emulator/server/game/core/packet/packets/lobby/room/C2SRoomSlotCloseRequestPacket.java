package com.jftse.emulator.server.game.core.packet.packets.lobby.room;

import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SRoomSlotCloseRequestPacket extends Packet {
    private byte slot;
    private boolean deactivate;

    public C2SRoomSlotCloseRequestPacket(Packet packet) {
        super(packet);

        this.slot = this.readByte();
        this.deactivate = this.readByte() == 1;
    }
}
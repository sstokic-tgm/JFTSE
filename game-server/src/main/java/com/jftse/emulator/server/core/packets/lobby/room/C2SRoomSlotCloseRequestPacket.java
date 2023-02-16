package com.jftse.emulator.server.core.packets.lobby.room;

import com.jftse.server.core.protocol.Packet;
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
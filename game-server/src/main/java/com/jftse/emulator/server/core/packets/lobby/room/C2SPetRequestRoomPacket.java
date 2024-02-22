package com.jftse.emulator.server.core.packets.lobby.room;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SPetRequestRoomPacket extends Packet {
    private byte slot;

    public C2SPetRequestRoomPacket(Packet packet) {
        super(packet);

        this.slot = this.readByte();
    }
}

package com.jftse.emulator.server.core.packets.lobby.room;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SRoomReadyChangeRequestPacket extends Packet {
    private boolean ready;

    public C2SRoomReadyChangeRequestPacket(Packet packet) {
        super(packet);

        this.ready = this.readByte() == 1;
    }
}
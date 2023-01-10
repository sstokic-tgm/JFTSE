package com.jftse.emulator.server.core.packets.lobby.room;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SRoomMapChangeRequestPacket extends Packet {
    private byte map;

    public C2SRoomMapChangeRequestPacket(Packet packet) {
        super(packet);

        this.map = this.readByte();
    }
}
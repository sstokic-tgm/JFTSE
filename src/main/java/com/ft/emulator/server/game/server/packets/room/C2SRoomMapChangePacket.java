package com.ft.emulator.server.game.server.packets.room;

import com.ft.emulator.server.game.server.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SRoomMapChangePacket extends Packet {

    private byte map;

    public C2SRoomMapChangePacket(Packet packet) {

        super(packet);

        this.map = this.readByte();
    }
}
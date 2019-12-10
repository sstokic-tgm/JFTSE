package com.ft.emulator.server.game.server.packets.room;

import com.ft.emulator.server.game.server.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SRoomReadyChangePacket extends Packet {

    private byte ready;

    public C2SRoomReadyChangePacket(Packet packet) {

        super(packet);

        this.ready = this.readByte();
    }
}
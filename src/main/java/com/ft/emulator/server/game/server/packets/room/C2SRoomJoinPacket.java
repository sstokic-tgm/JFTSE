package com.ft.emulator.server.game.server.packets.room;

import com.ft.emulator.server.game.server.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SRoomJoinPacket extends Packet {

    private char roomId;

    public C2SRoomJoinPacket(Packet packet) {

        super(packet);

        this.roomId = this.readChar();
    }
}
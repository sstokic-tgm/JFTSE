package com.ft.emulator.server.game.server.packets.room;

import com.ft.emulator.server.game.server.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SRoomPositionChange extends Packet {

    private char position;

    public C2SRoomPositionChange(Packet packet) {

        super(packet);

        this.position = this.readChar();
    }
}
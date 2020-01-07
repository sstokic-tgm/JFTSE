package com.ft.emulator.server.game.server.packets.chat;

import com.ft.emulator.server.game.server.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SChatLobbyReqPacket extends Packet {

    private char unk;
    private String message;

    public C2SChatLobbyReqPacket(Packet packet) {

        super(packet);

        this.unk = this.readChar();
        this.message = this.readUnicodeString().trim();
    }
}
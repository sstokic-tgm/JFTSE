package com.jftse.emulator.server.core.packets.chat;

import com.jftse.server.core.protocol.Packet;
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
        this.message = this.readUnicodeString();
    }
}

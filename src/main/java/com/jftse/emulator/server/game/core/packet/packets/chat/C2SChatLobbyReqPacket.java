package com.jftse.emulator.server.game.core.packet.packets.chat;

import com.jftse.emulator.server.networking.packet.Packet;
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

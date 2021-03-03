package com.jftse.emulator.server.game.core.packet.packets.chat;

import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SWhisperReqPacket extends Packet {
    private char unk;
    private String receiverName;
    private String message;

    public C2SWhisperReqPacket(Packet packet) {
        super(packet);

        this.unk = this.readChar();
        this.receiverName = this.readUnicodeString();
        this.message = this.readUnicodeString();
    }
}

package com.jftse.emulator.server.core.packets.chat;

import com.jftse.server.core.protocol.Packet;
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
        this.receiverName = this.readString();
        this.message = this.readString();
    }
}

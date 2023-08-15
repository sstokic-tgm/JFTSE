package com.jftse.emulator.server.core.packets.chat;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SChatRoomReqPacket extends Packet {
    private byte type;
    private String message;

    public C2SChatRoomReqPacket(Packet packet) {
        super(packet);

        this.type = this.readByte();
        this.message = this.readUnicodeString();
    }
}

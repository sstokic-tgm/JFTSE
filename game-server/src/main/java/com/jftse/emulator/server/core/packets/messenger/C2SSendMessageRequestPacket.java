package com.jftse.emulator.server.core.packets.messenger;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SSendMessageRequestPacket extends Packet {
    private String receiverName;
    private String message;

    public C2SSendMessageRequestPacket(Packet packet) {
        super(packet);

        this.receiverName = this.readUnicodeString();
        this.message = this.readUnicodeString();
    }
}

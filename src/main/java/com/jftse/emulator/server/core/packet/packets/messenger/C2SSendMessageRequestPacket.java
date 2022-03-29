package com.jftse.emulator.server.core.packet.packets.messenger;

import com.jftse.emulator.server.networking.packet.Packet;
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

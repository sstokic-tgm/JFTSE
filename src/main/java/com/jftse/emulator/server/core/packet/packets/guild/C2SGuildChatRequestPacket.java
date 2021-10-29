package com.jftse.emulator.server.core.packet.packets.guild;

import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SGuildChatRequestPacket extends Packet {
    private String message;

    public C2SGuildChatRequestPacket(Packet packet) {
        super(packet);

        this.message = this.readUnicodeString();
    }
}
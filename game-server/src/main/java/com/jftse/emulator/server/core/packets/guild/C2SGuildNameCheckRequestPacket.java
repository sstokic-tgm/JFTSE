package com.jftse.emulator.server.core.packets.guild;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SGuildNameCheckRequestPacket extends Packet {
    private String name;

    public C2SGuildNameCheckRequestPacket(Packet packet) {
        super(packet);

        this.name = this.readUnicodeString();
    }
}
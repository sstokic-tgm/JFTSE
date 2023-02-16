package com.jftse.emulator.server.core.packet.packets.guild;

import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SGuildSearchRequestPacket extends Packet {
    private byte searchType;
    private int number;
    private String name;

    public C2SGuildSearchRequestPacket(Packet packet) {
        super(packet);

        this.searchType = this.readByte();
        this.number = this.readInt();

        if (this.searchType == 1)
            this.name = this.readUnicodeString();
    }
}
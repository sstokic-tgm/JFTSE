package com.jftse.emulator.server.core.packets.guild;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SGuildListRequestPacket extends Packet {
    private int page;

    public C2SGuildListRequestPacket(Packet packet) {
        super(packet);

        this.page = this.readInt();
    }
}
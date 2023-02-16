package com.jftse.emulator.server.core.packet.packets.guild;

import com.jftse.emulator.server.networking.packet.Packet;
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
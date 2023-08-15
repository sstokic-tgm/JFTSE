package com.jftse.emulator.server.core.packets.guild;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SGuildReserveMemberDataRequestPacket extends Packet {
    private int page;

    public C2SGuildReserveMemberDataRequestPacket(Packet packet) {
        super(packet);

        this.page = this.readInt();
    }
}
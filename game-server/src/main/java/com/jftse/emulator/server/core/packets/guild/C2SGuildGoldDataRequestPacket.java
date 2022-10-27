package com.jftse.emulator.server.core.packets.guild;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SGuildGoldDataRequestPacket extends Packet {

    public C2SGuildGoldDataRequestPacket(Packet packet) {
        super(packet);
    }
}
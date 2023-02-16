package com.jftse.emulator.server.core.packets.guild;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SGuildNoticeRequestPacket extends Packet {

    public C2SGuildNoticeRequestPacket(Packet packet) {
        super(packet);
    }
}
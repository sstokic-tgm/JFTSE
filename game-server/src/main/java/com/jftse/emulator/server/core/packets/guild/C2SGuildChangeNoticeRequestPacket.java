package com.jftse.emulator.server.core.packets.guild;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SGuildChangeNoticeRequestPacket extends Packet {
    private String notice;

    public C2SGuildChangeNoticeRequestPacket(Packet packet) {
        super(packet);

        this.notice = this.readUnicodeString();
    }
}
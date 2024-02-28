package com.jftse.emulator.server.core.packets;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SSessionTimePacket extends Packet {
    private Integer accountSessionTime;
    private Integer playerSessionTime;

    public C2SSessionTimePacket(Packet packet) {
        super(packet);

        this.accountSessionTime = this.readInt();
        this.playerSessionTime = this.readInt();
    }
}

package com.jftse.emulator.server.core.packets.chat.house;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SShakeTreeRequestPacket extends Packet {
    short xPos;
    short yPos;

    public C2SShakeTreeRequestPacket(Packet packet) {
        super(packet);

        this.xPos = this.readShort();
        this.yPos = this.readShort();
    }
}

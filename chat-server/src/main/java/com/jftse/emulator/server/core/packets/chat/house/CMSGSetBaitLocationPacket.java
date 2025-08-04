package com.jftse.emulator.server.core.packets.chat.house;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CMSGSetBaitLocationPacket extends Packet {
    private float x;
    private float z;
    private float y;

    public CMSGSetBaitLocationPacket(Packet packet) {
        super(packet);

        this.x = this.readFloat();
        this.z = this.readFloat();
        this.y = this.readFloat();
    }
}

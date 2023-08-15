package com.jftse.emulator.server.core.packets.chat.square;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SChatSquareMovePacket extends Packet {
    private byte unk1;
    private float x;
    private float y;
    private float x2;
    private float y2;

    public C2SChatSquareMovePacket(Packet packet) {
        super(packet);

        this.unk1 = this.readByte();
        this.x = this.readFloat();
        this.y = this.readFloat();
        this.x2 = this.readFloat();
        this.y2 = this.readFloat();
    }
}

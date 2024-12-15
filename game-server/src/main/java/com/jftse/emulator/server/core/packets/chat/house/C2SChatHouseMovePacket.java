package com.jftse.emulator.server.core.packets.chat.house;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SChatHouseMovePacket extends Packet {
    private byte unk1;
    private byte unk2;
    private short x;
    private short y;
    private byte animationType;
    private byte unk3;

    public C2SChatHouseMovePacket(Packet packet) {
        super(packet);

        this.unk1 = this.readByte();
        this.unk2 = this.readByte();
        this.x = this.readShort();
        this.y = this.readShort();
        this.animationType = this.readByte();
        this.unk3 = this.readByte();
    }
}

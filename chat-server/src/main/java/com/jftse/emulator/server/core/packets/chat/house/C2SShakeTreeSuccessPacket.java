package com.jftse.emulator.server.core.packets.chat.house;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SShakeTreeSuccessPacket extends Packet {
    private byte song;
    private short score;

    public C2SShakeTreeSuccessPacket(Packet packet) {
        super(packet);

        this.song = this.readByte();
        this.score = this.readShort();
    }
}

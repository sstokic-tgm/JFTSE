package com.jftse.emulator.server.core.packets.chat.house;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SChatHousePositionPacket extends Packet {
    private byte level;
    private int x;
    private int y;

    public C2SChatHousePositionPacket(Packet packet) {
        super(packet);

        this.level = this.readByte();
        this.x = this.readInt();
        this.y = this.readInt();
    }
}

package com.jftse.emulator.server.core.packets.chat.house;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CChatHousePositionPacket extends Packet {
    public S2CChatHousePositionPacket(short position, byte level, int x, int y) {
        super(PacketOperations.S2CChatHousePosition);

        this.write(position);
        this.write(level);
        this.write(x);
        this.write(y);
    }
}

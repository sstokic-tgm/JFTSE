package com.jftse.emulator.server.core.packets.chat.square;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CChatSquareMovePacket extends Packet {
    public S2CChatSquareMovePacket(short position, byte unk1, float x, float y) {
        super(PacketOperations.S2CChatSquareMove);

        this.write(position);
        this.write(unk1);
        this.write(x);
        this.write(y);
    }
}

package com.jftse.emulator.server.core.packets.chat.house;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CShakeTreeAnswerPacket extends Packet {
    public S2CShakeTreeAnswerPacket(short position, short x, short y, boolean available) {
        super(PacketOperations.S2CShakeTreeAnswer);

        this.write(position);
        this.write(x);
        this.write(y);
        this.write(available);
    }
}

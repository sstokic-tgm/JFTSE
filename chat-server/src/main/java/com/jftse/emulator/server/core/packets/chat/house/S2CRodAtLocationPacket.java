package com.jftse.emulator.server.core.packets.chat.house;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CRodAtLocationPacket extends Packet {
    public S2CRodAtLocationPacket(short playerPosition, float x, float z, float y) {
        super(PacketOperations.S2CRodAtLocation);

        this.write(playerPosition);
        this.write(x);
        this.write(z);
        this.write(y);
    }
}

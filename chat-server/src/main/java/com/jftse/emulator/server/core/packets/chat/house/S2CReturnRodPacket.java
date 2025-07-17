package com.jftse.emulator.server.core.packets.chat.house;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CReturnRodPacket extends Packet {
    public S2CReturnRodPacket(short playerPosition) {
        super(PacketOperations.S2CReturnRod);

        this.write(playerPosition);
    }
}

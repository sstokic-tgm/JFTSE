package com.jftse.emulator.server.core.packets.player;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CCouplePointsDataPacket extends Packet {
    public S2CCouplePointsDataPacket(int couplePoints) {
        super(PacketOperations.S2CCouplePoints);

        this.write(couplePoints);
    }
}

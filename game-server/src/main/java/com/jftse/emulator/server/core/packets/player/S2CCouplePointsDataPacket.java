package com.jftse.emulator.server.core.packets.player;

import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;

public class S2CCouplePointsDataPacket extends Packet {
    public S2CCouplePointsDataPacket(int couplePoints) {
        super(PacketOperations.S2CCouplePoints.getValue());

        this.write(couplePoints);
    }
}

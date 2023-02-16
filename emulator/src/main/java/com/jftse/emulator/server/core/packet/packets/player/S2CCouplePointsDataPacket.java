package com.jftse.emulator.server.core.packet.packets.player;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CCouplePointsDataPacket extends Packet {
    public S2CCouplePointsDataPacket(int couplePoints) {
        super(PacketOperations.S2CCouplePoints.getValueAsChar());

        this.write(couplePoints);
    }
}

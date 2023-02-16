package com.jftse.emulator.server.core.packet.packets.player;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CPlayerLevelExpPacket extends Packet {
    public S2CPlayerLevelExpPacket(byte level, int expValue) {
        super(PacketOperations.S2CPlayerLevelExpData.getValueAsChar());

        this.write(level);
        this.write(expValue);
    }
}

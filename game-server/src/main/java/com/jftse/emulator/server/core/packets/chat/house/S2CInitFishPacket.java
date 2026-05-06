package com.jftse.emulator.server.core.packets.chat.house;

import com.jftse.emulator.server.core.life.housing.Fish;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CInitFishPacket extends Packet {
    public S2CInitFishPacket(Fish fish) {
        super(PacketOperations.S2CInitFish);

        this.write(fish.getId());
        this.write(fish.getX());
        this.write(fish.getZ());
        this.write(fish.getY());
        this.write(fish.getRotation());
        this.write(fish.getModel());
    }
}


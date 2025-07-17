package com.jftse.emulator.server.core.packets.chat.house;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CInitFishPacket extends Packet {
    public S2CInitFishPacket(short fishId, float x, float z, float y, float rotation, byte fishModel) {
        super(PacketOperations.S2CInitFish);

        this.write(fishId);
        this.write(x);
        this.write(z);
        this.write(y);
        this.write(rotation);
        this.write(fishModel);
    }
}

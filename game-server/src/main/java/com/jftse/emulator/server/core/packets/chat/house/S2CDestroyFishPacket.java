package com.jftse.emulator.server.core.packets.chat.house;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CDestroyFishPacket extends Packet {
    public S2CDestroyFishPacket(short fishId) {
        super(PacketOperations.S2CDestroyFish);

        this.write(fishId);
    }
}

package com.jftse.emulator.server.core.packets.messenger;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CRemoveCoupleRingPacket extends Packet {
    public S2CRemoveCoupleRingPacket() {
        super(PacketOperations.S2CRemoveCoupleRing);
    }
}

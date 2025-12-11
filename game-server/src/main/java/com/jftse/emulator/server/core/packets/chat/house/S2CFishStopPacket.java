package com.jftse.emulator.server.core.packets.chat.house;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CFishStopPacket extends Packet {
    public S2CFishStopPacket(short fishId, byte state) {
        super(PacketOperations.S2CFishStop);

        this.write(fishId);
        this.write(state);
    }
}

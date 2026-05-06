package com.jftse.emulator.server.core.packets.chat.house;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CFishSetSpeedPacket extends Packet {
    public S2CFishSetSpeedPacket(short fishId, short unk0, float speed) {
        super(PacketOperations.S2CFishSetSpeed);

        this.write(fishId);
        this.write(unk0);
        this.write(speed);
    }
}

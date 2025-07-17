package com.jftse.emulator.server.core.packets.chat.house;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CFishMovePacket extends Packet {
    public S2CFishMovePacket(short fishId, byte state, float unk0, float unk1, float speed) {
        super(PacketOperations.S2CFishMove);

        this.write(fishId);
        this.write(state);
        this.write(unk0);
        this.write(unk1);
        this.write(speed);
    }
}

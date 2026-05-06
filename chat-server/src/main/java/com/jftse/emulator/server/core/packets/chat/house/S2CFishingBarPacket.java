package com.jftse.emulator.server.core.packets.chat.house;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CFishingBarPacket extends Packet {
    public S2CFishingBarPacket(short playerPosition, short fishId, float barSpeed, byte unk0) {
        super(PacketOperations.S2CFishingBar);

        this.write(playerPosition);
        this.write(fishId);
        this.write(barSpeed);
        this.write(unk0);
    }
}

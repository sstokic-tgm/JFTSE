package com.jftse.emulator.server.core.packets;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;

@Getter
public class C2CSpiderMinePlacedPacket extends Packet {
    private final int position;
    private final boolean shouldPlace;
    private final short spiderMineId;
    private final double xPos;
    private final double yPos;

    public C2CSpiderMinePlacedPacket(Packet packet) {
        super(packet);

        this.position = this.readInt();
        this.shouldPlace = this.readBoolean();
        this.spiderMineId = this.readShort();
        short xPos = this.readShort();
        short yPos = this.readShort();
        this.xPos = (double) xPos * 0.009999999776482582;
        this.yPos = 0.009999999776482582 * (double) yPos;
    }
}

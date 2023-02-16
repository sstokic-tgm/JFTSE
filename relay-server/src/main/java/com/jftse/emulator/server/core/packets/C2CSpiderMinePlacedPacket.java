package com.jftse.emulator.server.core.packets;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;

@Getter
public class C2CSpiderMinePlacedPacket extends Packet {
    private int position;
    private boolean shouldPlace;
    private short spiderMineId;
    private double xPos;
    private double yPos;

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

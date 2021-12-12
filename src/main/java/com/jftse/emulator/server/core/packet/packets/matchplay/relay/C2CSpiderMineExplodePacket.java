package com.jftse.emulator.server.core.packet.packets.matchplay.relay;

import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;

@Getter
public class C2CSpiderMineExplodePacket extends Packet {
    private byte targetPosition;
    private short spiderMineId;

    public C2CSpiderMineExplodePacket(Packet packet) {
        super(packet);

        this.targetPosition = this.readByte();
        this.spiderMineId = this.readShort();
    }
}

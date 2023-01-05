package com.jftse.emulator.server.core.packets;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2CSpiderMineExplodePacket extends Packet {
    private byte targetPosition;
    private short spiderMineId;

    public C2CSpiderMineExplodePacket(byte targetPosition, short spiderMineId) {
        super(PacketOperations.C2CSpiderMineExplodePacket);

        this.targetPosition = targetPosition;
        this.spiderMineId = spiderMineId;

        this.write(targetPosition);
        this.write(spiderMineId);
    }

    public C2CSpiderMineExplodePacket(Packet packet) {
        super(packet);

        this.targetPosition = this.readByte();
        this.spiderMineId = this.readShort();
    }
}

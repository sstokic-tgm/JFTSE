package com.jftse.emulator.server.core.packets.matchplay;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SMatchplayClientSkillHitsTarget extends Packet {
    private byte targetPosition;
    private byte skillId;
    private int xKnockbackPosition;
    private int yKnockbackPosition;

    public C2SMatchplayClientSkillHitsTarget(Packet packet) {
        super(packet);

        this.readByte(); // Unk
        this.targetPosition = this.readByte();
        this.readByte();
        this.readInt();
        this.readByte();
        this.skillId = this.readByte();
        this.xKnockbackPosition = this.readInt();
        this.yKnockbackPosition = this.readInt();
        this.readByte(); // Unk
    }
}

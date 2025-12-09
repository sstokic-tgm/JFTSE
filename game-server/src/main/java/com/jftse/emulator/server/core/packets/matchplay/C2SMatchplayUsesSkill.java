package com.jftse.emulator.server.core.packets.matchplay;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SMatchplayUsesSkill extends Packet {
    private byte attackerPosition;
    private byte targetPosition;
    private byte skillIndex;
    private int someKindOfTargeting;
    private boolean isQuickSlot;
    private byte quickSlotIndex;
    private byte seed;
    private float xTarget;
    private float zTarget;
    private float yTarget;

    public C2SMatchplayUsesSkill(Packet packet) {
        super(packet);

        this.attackerPosition = this.readByte();
        this.targetPosition = this.readByte();
        this.isQuickSlot = this.readByte() == 1;
        this.quickSlotIndex = this.readByte();
        this.skillIndex = this.readByte();
        this.someKindOfTargeting = this.readInt();
        this.readInt(); // Unk
        this.seed = this.readByte();
        this.xTarget = this.readFloat();
        this.zTarget = this.readFloat();
        this.yTarget = this.readFloat();
    }
}

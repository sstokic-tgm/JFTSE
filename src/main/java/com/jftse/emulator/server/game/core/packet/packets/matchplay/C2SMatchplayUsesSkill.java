package com.jftse.emulator.server.game.core.packet.packets.matchplay;

import com.jftse.emulator.server.networking.packet.Packet;
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

        this.attackerPosition = packet.readByte();
        this.targetPosition = packet.readByte();
        this.isQuickSlot = packet.readByte() == 1;
        this.quickSlotIndex = packet.readByte();
        this.skillIndex = packet.readByte();
        this.someKindOfTargeting = packet.readInt();
        packet.readInt(); // Unk
        this.seed = packet.readByte();
        this.xTarget = packet.readFloat();
        this.zTarget = packet.readFloat();
        this.yTarget = packet.readFloat();
    }
}

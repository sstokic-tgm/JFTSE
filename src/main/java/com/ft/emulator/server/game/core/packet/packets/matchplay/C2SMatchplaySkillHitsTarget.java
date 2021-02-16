package com.ft.emulator.server.game.core.packet.packets.matchplay;

import com.ft.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SMatchplaySkillHitsTarget extends Packet {
    private byte targetPosition;
    private byte skillId;
    private byte damageType;
    private int xKnockbackPosition;
    private int yKnockbackPosition;

    public C2SMatchplaySkillHitsTarget(Packet packet) {
        super(packet);

        packet.readByte(); // Unk
        packet.readByte(); // Unk
        packet.readByte(); // Unk
        this.targetPosition = packet.readByte();
        packet.readShort(); // Unk
        this.skillId = packet.readByte();

        // Unknown 10 bytes (types are wrong to probably)
        packet.readInt();

        packet.readShort();
        this.damageType = packet.readByte();
        packet.readByte();

        packet.readShort();

        this.xKnockbackPosition = packet.readInt();
        this.yKnockbackPosition = packet.readInt();

        packet.readByte(); // Unk
    }
}

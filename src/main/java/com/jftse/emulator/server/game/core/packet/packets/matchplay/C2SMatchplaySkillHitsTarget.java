package com.jftse.emulator.server.game.core.packet.packets.matchplay;

import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SMatchplaySkillHitsTarget extends Packet {
    private byte attackerPosition;
    private byte targetPosition;
    private byte skillId;
    private byte damageType;
    private int xKnockbackPosition;
    private int yKnockbackPosition;
    private byte attackerBuffId;
    private byte receiverBuffId;

    public C2SMatchplaySkillHitsTarget(Packet packet) {
        super(packet);

        this.attackerPosition = packet.readByte();
        packet.readByte(); // Unk
        packet.readByte(); // Unk
        this.targetPosition = packet.readByte();
        packet.readShort(); // Unk
        this.skillId = packet.readByte();

        // 0=Str, 1=DEF, 2=Projectile speed, 3=Ball damage, 4=DMG and Projectile speed
        // 5=Charge shot speed, 6=Movement speed, 7=Ball spin
        this.attackerBuffId = packet.readByte();
        packet.readShort(); // Unk
        this.receiverBuffId = packet.readByte();
        packet.readShort(); // Unk;
        this.damageType = packet.readByte();
        packet.readByte(); // Unk
        packet.readShort(); // Unk
        this.xKnockbackPosition = packet.readInt();
        this.yKnockbackPosition = packet.readInt();
        packet.readByte(); // Unk
    }
}

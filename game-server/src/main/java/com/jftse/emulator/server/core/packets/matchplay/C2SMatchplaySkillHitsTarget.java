package com.jftse.emulator.server.core.packets.matchplay;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SMatchplaySkillHitsTarget extends Packet {
    private short attackerPosition;
    private byte unk0;
    private short targetPosition;
    private byte unk1;
    private byte skillId;
    private byte attackerBuffId1;
    private byte attackerBuffId2;
    private byte unk3;
    private byte receiverBuffId1;
    private byte unk4;
    private byte receiverBuffId2;
    private byte damageType;
    private byte unk6;
    private boolean applySkillEffect;
    private byte unk7;
    private int xKnockbackPosition;
    private int yKnockbackPosition;
    private byte unk8;

    public C2SMatchplaySkillHitsTarget(Packet packet) {
        super(packet);

        this.attackerPosition = packet.readShort();
        this.unk0 = packet.readByte();
        this.targetPosition = packet.readShort();
        this.unk1 = packet.readByte();
        this.skillId = packet.readByte();

        // 0=Str, 1=DEF, 2=Projectile speed, 3=Ball damage, 4=DMG and Projectile speed
        // 5=Charge shot speed, 6=Movement speed, 7=Ball spin
        this.attackerBuffId1 = packet.readByte();
        this.attackerBuffId2 = packet.readByte();
        this.unk3 = packet.readByte();
        this.receiverBuffId1 = packet.readByte();
        this.unk4 = packet.readByte();
        this.receiverBuffId2 = packet.readByte();
        this.damageType = packet.readByte();
        this.unk6 = packet.readByte();
        this.applySkillEffect = packet.readByte() == 0;
        this.unk7 = packet.readByte();
        this.xKnockbackPosition = packet.readInt();
        this.yKnockbackPosition = packet.readInt();
        this.unk8 = packet.readByte();
    }

    @Override
    public String toString() {
        return "C2SMatchplaySkillHitsTarget{" +
                "attackerPosition=" + attackerPosition +
                ", unk0=" + unk0 +
                ", targetPosition=" + targetPosition +
                ", unk1=" + unk1 +
                ", skillId=" + skillId +
                ", attackerBuffId1=" + attackerBuffId1 +
                ", attackerBuffId2=" + attackerBuffId2 +
                ", unk3=" + unk3 +
                ", receiverBuffId1=" + receiverBuffId1 +
                ", unk4=" + unk4 +
                ", receiverBuffId2=" + receiverBuffId2 +
                ", damageType=" + damageType +
                ", unk6=" + unk6 +
                ", applySkillEffect=" + applySkillEffect +
                ", unk7=" + unk7 +
                ", xKnockbackPosition=" + xKnockbackPosition +
                ", yKnockbackPosition=" + yKnockbackPosition +
                ", unk8=" + unk8 +
                '}';
    }
}

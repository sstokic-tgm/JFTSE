package com.jftse.emulator.server.game.core.packet.packets.matchplay;

import com.jftse.emulator.server.networking.packet.Packet;
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

        packet.readByte(); // Unk
        this.targetPosition = packet.readByte();
        packet.readByte();
        packet.readInt();
        packet.readByte();
        this.skillId = packet.readByte();
        this.xKnockbackPosition = packet.readInt();
        this.yKnockbackPosition = packet.readInt();
        packet.readByte(); // Unk
    }
}

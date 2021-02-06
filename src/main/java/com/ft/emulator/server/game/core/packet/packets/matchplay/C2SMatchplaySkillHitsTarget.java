package com.ft.emulator.server.game.core.packet.packets.matchplay;

import com.ft.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SMatchplaySkillHitsTarget extends Packet {
    private byte targetPosition;
    private byte skillHitAnimation;

    public C2SMatchplaySkillHitsTarget(Packet packet) {
        super(packet);

        packet.readByte(); // Unk
        packet.readByte(); // Unk
        packet.readByte(); // Unk
        this.targetPosition = packet.readByte();
        packet.readShort(); // Unk
        this.skillHitAnimation = packet.readByte();
        // 19 unknown bytes follow here
    }
}

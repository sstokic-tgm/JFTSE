package com.ft.emulator.server.game.core.packet.packets.matchplay;

import com.ft.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SMatchplayUseSkill extends Packet {
    private byte playerPosition;
    private byte targetPosition;
    private byte skillIndex;

    public C2SMatchplayUseSkill(Packet packet) {
        super(packet);

        this.playerPosition = packet.readByte();
        this.targetPosition = packet.readByte();
        packet.readShort(); // Unk
        this.skillIndex = packet.readByte();
        packet.readInt(); // Unk
        packet.readInt(); // Unk
        packet.readShort(); // Damage?
        // 12 more unknown bytes follow (Contains maybe positions)
    }
}

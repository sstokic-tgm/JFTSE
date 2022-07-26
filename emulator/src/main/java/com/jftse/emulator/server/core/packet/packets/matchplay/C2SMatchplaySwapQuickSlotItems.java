package com.jftse.emulator.server.core.packet.packets.matchplay;

import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SMatchplaySwapQuickSlotItems extends Packet {
    private byte targetLeftSlotSkill;
    private byte targetRightSlotSkill;

    public C2SMatchplaySwapQuickSlotItems(Packet packet) {
        super(packet);

        packet.readByte(); // Unk
        packet.readByte(); // Unk
        packet.readByte(); // Unk
        packet.readByte(); // Unk
        this.targetLeftSlotSkill = packet.readByte();
        packet.readInt(); // Unk
        packet.readByte(); // Unk
        packet.readByte(); // Unk
        packet.readByte(); // Unk
        this.targetRightSlotSkill = packet.readByte();

         // 7 more unknown bytes follow here
    }
}

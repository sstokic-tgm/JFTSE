package com.jftse.emulator.server.core.packets.matchplay;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SMatchplaySwapQuickSlotItems extends Packet {
    private byte targetLeftSlotSkill;
    private byte targetRightSlotSkill;

    public C2SMatchplaySwapQuickSlotItems(Packet packet) {
        super(packet);

        this.readByte(); // Unk
        this.readByte(); // Unk
        this.readByte(); // Unk
        this.readByte(); // Unk
        this.targetLeftSlotSkill = this.readByte();
        this.readInt(); // Unk
        this.readByte(); // Unk
        this.readByte(); // Unk
        this.readByte(); // Unk
        this.targetRightSlotSkill = this.readByte();

         // 7 more unknown bytes follow here
    }
}

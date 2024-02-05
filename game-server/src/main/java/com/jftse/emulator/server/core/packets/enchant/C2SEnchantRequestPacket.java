package com.jftse.emulator.server.core.packets.enchant;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SEnchantRequestPacket extends Packet {
    private int itemPocketId;
    private int elementPocketId;
    private int jewelPocketId;

    public C2SEnchantRequestPacket(Packet packet) {
        super(packet);

        this.itemPocketId = this.readInt();
        this.elementPocketId = this.readInt();
        this.jewelPocketId = this.readInt();
    }
}

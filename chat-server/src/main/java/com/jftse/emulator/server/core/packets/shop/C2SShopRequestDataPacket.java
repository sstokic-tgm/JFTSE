package com.jftse.emulator.server.core.packets.shop;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SShopRequestDataPacket extends Packet {
    private byte category;
    private byte part;
    private byte player;
    private byte page;

    public C2SShopRequestDataPacket(Packet packet) {
        super(packet);

        this.category = this.readByte();
        this.part = this.readByte();
        this.player = this.readByte();
        this.page = this.readByte();
    }
}

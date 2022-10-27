package com.jftse.emulator.server.core.packets.shop;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SShopRequestDataPreparePacket extends Packet {
    private byte category;
    private byte part;
    private byte player;

    public C2SShopRequestDataPreparePacket(Packet packet) {
        super(packet);

        this.category = this.readByte();
        this.part = this.readByte();
        this.player = this.readByte();
    }
}

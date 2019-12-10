package com.ft.emulator.server.game.server.packets.shop;

import com.ft.emulator.server.game.server.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SShopRequestDataPacket extends Packet {

    private byte category;
    private byte part;
    private byte character;
    private byte page;

    public C2SShopRequestDataPacket(Packet packet) {

        super(packet);

        this.category = this.readByte();
        this.part = this.readByte();
        this.character = this.readByte();
        this.page = this.readByte();
    }
}
package com.ft.emulator.server.game.server.packets.shop;

import com.ft.emulator.server.game.server.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SShopBuyPacket extends Packet {

    private byte unk0; // maybe buy/gift
    private int itemId;
    private byte option; // days, count

    public C2SShopBuyPacket(Packet packet) {

        super(packet);

        this.unk0 = this.readByte();
        this.itemId = this.readInt();
        this.option = this.readByte();
    }
}
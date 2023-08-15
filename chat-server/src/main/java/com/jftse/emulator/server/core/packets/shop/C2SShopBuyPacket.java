package com.jftse.emulator.server.core.packets.shop;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class C2SShopBuyPacket extends Packet {
    private byte itemCount;
    private Map<Integer, Byte> itemList;

    public C2SShopBuyPacket(Packet packet) {
        super(packet);

        itemList = new HashMap<>();

        this.itemCount = this.readByte();

        for (byte i = 0; i < itemCount; ++i) {
            int itemId = this.readInt();
            byte option = this.readByte();

            itemList.put(itemId, option);
        }
    }
}

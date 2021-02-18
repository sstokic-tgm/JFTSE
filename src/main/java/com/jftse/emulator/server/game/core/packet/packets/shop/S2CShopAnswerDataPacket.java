package com.jftse.emulator.server.game.core.packet.packets.shop;

import com.jftse.emulator.server.database.model.item.Product;
import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;

public class S2CShopAnswerDataPacket extends Packet {

    public S2CShopAnswerDataPacket(int pageCount, List<Product> productList) {
        super(PacketID.S2CShopAnswerData);

        this.write(pageCount);

        this.write((char) productList.size());

        for (Product product : productList) {
            this.write(product.getProductIndex());
            this.write(product.getPriceType().equals("GOLD") ? (byte) 0 : (byte) 1);
            this.write(product.getGoldBack());
            this.write(product.getUse0());
            this.write(product.getUse1());
            this.write(product.getUse2());
            this.write(product.getPrice0());
            this.write(product.getPrice1());
            this.write(product.getPrice2());
            this.write(product.getOldPrice0());
            this.write(product.getOldPrice1());
            this.write(product.getOldPrice2());
        }
    }
}

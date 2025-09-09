package com.jftse.emulator.server.core.packets.shop;

import com.jftse.server.core.constants.ShopItemFlags;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.item.Product;

import java.util.List;

public class S2CShopAnswerDataPacket extends Packet {

    public S2CShopAnswerDataPacket(int pageCount, List<Product> productList) {
        super(PacketOperations.S2CShopAnswerData);

        this.write(pageCount);

        this.write((char) productList.size());

        for (Product product : productList) {
            this.write(product.getProductIndex());

            byte shopItemFlags = ShopItemFlags.getPriceTypeFlag(product.getPriceType());

            if (product.getCouplePrice() != 0) {
                shopItemFlags = ShopItemFlags.add(shopItemFlags, ShopItemFlags.COUPLE);
            }

            if (product.getNoBuy() != null && product.getNoBuy()) {
                shopItemFlags = ShopItemFlags.add(shopItemFlags, ShopItemFlags.NO_BUY);
            }

            this.write(shopItemFlags);
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

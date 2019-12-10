package com.ft.emulator.server.game.server.packets.shop;

import com.ft.emulator.server.database.model.item.Product;
import com.ft.emulator.server.game.server.Packet;
import com.ft.emulator.server.game.server.PacketID;

import java.util.List;

public class S2CShopAnswerDataPacket extends Packet {

    public S2CShopAnswerDataPacket(int pageCount, List<Product> products) {

        super(PacketID.S2CShopAnswerData);

        this.write(pageCount);

        this.write((char)products.size());
        for(Product product : products) {

	    this.write(Math.toIntExact(product.getProductIndex()));
	    this.write(product.getPriceType().equals("GOLD") ? (byte)0 : (byte)1); // some kind of type, it's weird, it can be gold, mint, new etc
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
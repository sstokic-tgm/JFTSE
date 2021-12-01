package com.jftse.emulator.server.core.handler.game.shop;

import com.jftse.emulator.common.utilities.BitKit;
import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.shop.C2SShopRequestDataPacket;
import com.jftse.emulator.server.core.packet.packets.shop.S2CShopAnswerDataPacket;
import com.jftse.emulator.server.core.service.ProductService;
import com.jftse.emulator.server.database.model.item.Product;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;

public class ShopRequestDataPacketHandler extends AbstractHandler {
    private C2SShopRequestDataPacket shopRequestDataPacket;

    private final ProductService productService;

    public ShopRequestDataPacketHandler() {
        productService = ServiceManager.getInstance().getProductService();
    }

    @Override
    public boolean process(Packet packet) {
        shopRequestDataPacket = new C2SShopRequestDataPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        byte category = shopRequestDataPacket.getCategory();
        byte part = shopRequestDataPacket.getPart();
        byte player = shopRequestDataPacket.getPlayer();
        int page = BitKit.fromUnsignedInt(shopRequestDataPacket.getPage());

        List<Product> productList = productService.getProductList(category, part, player, page);

        S2CShopAnswerDataPacket shopAnswerDataPacket = new S2CShopAnswerDataPacket(productList.size(), productList);
        connection.sendTCP(shopAnswerDataPacket);
    }
}

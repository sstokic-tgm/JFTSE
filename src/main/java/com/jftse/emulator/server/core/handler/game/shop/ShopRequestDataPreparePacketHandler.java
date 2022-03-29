package com.jftse.emulator.server.core.handler.game.shop;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.shop.C2SShopRequestDataPreparePacket;
import com.jftse.emulator.server.core.packet.packets.shop.S2CShopAnswerDataPreparePacket;
import com.jftse.emulator.server.core.service.ProductService;
import com.jftse.emulator.server.networking.packet.Packet;

public class ShopRequestDataPreparePacketHandler extends AbstractHandler {
    private C2SShopRequestDataPreparePacket shopRequestDataPreparePacket;

    private final ProductService productService;

    public ShopRequestDataPreparePacketHandler() {
        productService = ServiceManager.getInstance().getProductService();
    }

    @Override
    public boolean process(Packet packet) {
        shopRequestDataPreparePacket = new C2SShopRequestDataPreparePacket(packet);
        return true;
    }

    @Override
    public void handle() {
        byte category = shopRequestDataPreparePacket.getCategory();
        byte part = shopRequestDataPreparePacket.getPart();
        byte player = shopRequestDataPreparePacket.getPlayer();

        int productListSize = productService.getProductListSize(category, part, player);

        S2CShopAnswerDataPreparePacket shopAnswerDataPreparePacket = new S2CShopAnswerDataPreparePacket(category, part, player, productListSize);
        connection.sendTCP(shopAnswerDataPreparePacket);
    }
}

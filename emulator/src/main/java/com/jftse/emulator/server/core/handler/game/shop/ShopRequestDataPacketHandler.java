package com.jftse.emulator.server.core.handler.game.shop;

import com.jftse.emulator.common.utilities.BitKit;
import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.shop.C2SShopRequestDataPacket;
import com.jftse.emulator.server.core.packet.packets.shop.S2CShopAnswerDataPacket;
import com.jftse.emulator.server.core.packet.packets.shop.S2CShopAnswerDataPreparePacket;
import com.jftse.emulator.server.core.service.ProductService;
import com.jftse.entities.database.model.item.Product;
import com.jftse.emulator.server.networking.packet.Packet;
import com.jftse.emulator.server.shared.module.Client;

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

        Client client = connection.getClient();
        if (client != null) {
            final boolean requestedShopDataPrepare = client.isRequestedShopDataPrepare();
            if (requestedShopDataPrepare) {
                client.setRequestedShopDataPrepare(false); // reset flag
            } else {
                int productListSize = productService.getProductListSize(category, part, player);

                S2CShopAnswerDataPreparePacket shopAnswerDataPreparePacket = new S2CShopAnswerDataPreparePacket(category, part, player, productListSize);
                connection.sendTCP(shopAnswerDataPreparePacket);
            }
        }

        S2CShopAnswerDataPacket shopAnswerDataPacket = new S2CShopAnswerDataPacket(productList.size(), productList);
        connection.sendTCP(shopAnswerDataPacket);
    }
}

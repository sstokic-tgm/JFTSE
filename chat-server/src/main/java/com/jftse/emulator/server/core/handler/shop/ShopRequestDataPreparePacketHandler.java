package com.jftse.emulator.server.core.handler.shop;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.ProductService;
import com.jftse.server.core.shared.packets.shop.CMSGShopDataPrepare;
import com.jftse.server.core.shared.packets.shop.SMSGShopDataPrepare;

@PacketId(CMSGShopDataPrepare.PACKET_ID)
public class ShopRequestDataPreparePacketHandler implements PacketHandler<FTConnection, CMSGShopDataPrepare> {
    private final ProductService productService;

    public ShopRequestDataPreparePacketHandler() {
        productService = ServiceManager.getInstance().getProductService();
    }

    @Override
    public void handle(FTConnection connection, CMSGShopDataPrepare packet) {
        byte category = packet.getCategory();
        byte part = packet.getPart();
        byte player = packet.getPlayer();

        int productListSize = productService.getProductListSize(category, part, player);

        FTClient client = connection.getClient();
        if (client != null) {
            final boolean requestedShopDataPrepare = client.isRequestedShopDataPrepare();
            if (!requestedShopDataPrepare) {
                client.setRequestedShopDataPrepare(true);
            }
        }

        SMSGShopDataPrepare response = SMSGShopDataPrepare.builder()
                .category(category)
                .part(part)
                .player(player)
                .size(productListSize)
                .build();
        connection.sendTCP(response);
    }
}

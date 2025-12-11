package com.jftse.emulator.server.core.handler.shop;

import com.jftse.emulator.common.utilities.BitKit;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.shop.S2CShopAnswerDataPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.item.Product;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.ProductService;
import com.jftse.server.core.shared.packets.shop.CMSGShopData;
import com.jftse.server.core.shared.packets.shop.SMSGShopDataPrepare;

import java.util.List;

@PacketId(CMSGShopData.PACKET_ID)
public class ShopRequestDataPacketHandler implements PacketHandler<FTConnection, CMSGShopData> {
    private final ProductService productService;

    public ShopRequestDataPacketHandler() {
        productService = ServiceManager.getInstance().getProductService();
    }

    @Override
    public void handle(FTConnection connection, CMSGShopData packet) {
        byte category = packet.getCategory();
        byte part = packet.getPart();
        byte player = packet.getPlayer();
        int page = BitKit.fromUnsignedInt(packet.getPage());

        int productListSize = productService.getProductListSize(category, part, player);

        FTClient client = connection.getClient();
        if (client != null) {
            final boolean requestedShopDataPrepare = client.isRequestedShopDataPrepare();
            if (requestedShopDataPrepare) {
                client.setRequestedShopDataPrepare(false); // reset flag
            } else {
                SMSGShopDataPrepare preparedData = SMSGShopDataPrepare.builder()
                        .category(category)
                        .part(part)
                        .player(player)
                        .size(productListSize)
                        .build();
                connection.sendTCP(preparedData);
            }
        }

        int pageCount = (productListSize - 1) / 6 + 1;
        for (int i = page; i <= pageCount; i++) {
            List<Product> productList = productService.getProductList(category, part, player, i);
            S2CShopAnswerDataPacket shopAnswerDataPacket = new S2CShopAnswerDataPacket(productList.size(), productList);
            connection.sendTCP(shopAnswerDataPacket);
        }
    }
}

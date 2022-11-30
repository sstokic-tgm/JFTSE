package com.jftse.emulator.server.core.handler.shop;

import com.jftse.emulator.server.core.packets.shop.C2SShopRequestDataPreparePacket;
import com.jftse.emulator.server.core.packets.shop.S2CShopAnswerDataPreparePacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.ProductService;

@PacketOperationIdentifier(PacketOperations.C2SShopRequestDataPrepare)
public class ShopRequestDataPreparePacketHandler extends AbstractPacketHandler {
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

        FTClient client = (FTClient) connection.getClient();
        if (client != null) {
            final boolean requestedShopDataPrepare = client.isRequestedShopDataPrepare();
            if (!requestedShopDataPrepare) {
                client.setRequestedShopDataPrepare(true);
            }
        }

        S2CShopAnswerDataPreparePacket shopAnswerDataPreparePacket = new S2CShopAnswerDataPreparePacket(category, part, player, productListSize);
        connection.sendTCP(shopAnswerDataPreparePacket);
    }
}

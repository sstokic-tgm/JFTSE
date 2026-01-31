package com.jftse.emulator.server.core.handler.gacha;

import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryItemCountPacket;
import com.jftse.emulator.server.core.packets.lottery.S2COpenGachaAnswerPacket;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.item.Product;
import com.jftse.entities.database.model.lottery.LotteryItemDto;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.LotteryService;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.service.PocketService;
import com.jftse.server.core.service.ProductService;
import com.jftse.server.core.shared.packets.gacha.CMSGOpenGacha;
import com.jftse.server.core.shared.packets.inventory.S2CInventoryItemRemoveAnswerPacket;

import java.util.List;

@PacketId(CMSGOpenGacha.PACKET_ID)
public class OpenGachaRequestPacketHandler implements PacketHandler<FTConnection, CMSGOpenGacha> {
    private final LotteryService lotteryService;
    private final ProductService productService;
    private final PlayerPocketService playerPocketService;
    private final PocketService pocketService;

    public OpenGachaRequestPacketHandler() {
        lotteryService = ServiceManager.getInstance().getLotteryService();
        productService = ServiceManager.getInstance().getProductService();
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        pocketService = ServiceManager.getInstance().getPocketService();
    }

    @Override
    public void handle(FTConnection connection, CMSGOpenGacha openGachaReqPacket) {
        if (!connection.getClient().hasPlayer()) {
            return;
        }

        long playerPocketId = openGachaReqPacket.getPlayerPocketId();
        int productIndex = openGachaReqPacket.getProductIndex();

        FTPlayer player = connection.getClient().getPlayer();
        Pocket pocket = pocketService.findById(player.getPocketId());

        Product product = productService.findProductByProductItemIndex(productIndex);
        if (product != null) {
            if (!product.getCategory().equals("LOTTERY")) {
                return;
            }

            List<LotteryItemDto> lotteryItemList = lotteryService.getLotteryItemsByGachaIndex(player.getPlayerType(), product.getItem0());

            PlayerPocket ppGacha = playerPocketService.getItemAsPocket(playerPocketId, pocket.getId());
            if (ppGacha != null) {
                ppGacha.setItemCount(ppGacha.getItemCount() - 1);
                PlayerPocket drawnItem = lotteryService.drawLottery(lotteryItemList, pocket);

                int itemCount = ppGacha.getItemCount();
                if (itemCount <= 0) {
                    pocketService.decrementPocketBelongings(player.getPocketId());
                    playerPocketService.remove(ppGacha.getId());

                    // if current count is 0 remove the item
                    S2CInventoryItemRemoveAnswerPacket inventoryItemRemoveAnswerPacket = new S2CInventoryItemRemoveAnswerPacket(Math.toIntExact(ppGacha.getId()));
                    connection.sendTCP(inventoryItemRemoveAnswerPacket);
                }
                else {
                    ppGacha.setItemCount(itemCount);
                    ppGacha = playerPocketService.save(ppGacha);

                    S2CInventoryItemCountPacket inventoryItemCountPacket = new S2CInventoryItemCountPacket(ppGacha);
                    connection.sendTCP(inventoryItemCountPacket);
                }

                S2COpenGachaAnswerPacket openGachaAnswerPacket = new S2COpenGachaAnswerPacket(List.of(drawnItem));
                connection.sendTCP(openGachaAnswerPacket);
            }
        }
    }
}

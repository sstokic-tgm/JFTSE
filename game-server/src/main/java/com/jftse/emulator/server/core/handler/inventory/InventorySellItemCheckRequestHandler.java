package com.jftse.emulator.server.core.handler.inventory;

import com.jftse.emulator.common.utilities.StreamUtils;
import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.inventory.S2CInventorySellItemAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.service.PlayerService;
import com.jftse.server.core.service.PocketService;
import com.jftse.server.core.shared.packets.inventory.CMSGInventorySellItemCheck;
import com.jftse.server.core.shared.packets.inventory.SMSGInventorySellItemCheck;
import com.jftse.server.core.shared.packets.shop.SMSGSetMoney;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@PacketId(CMSGInventorySellItemCheck.PACKET_ID)
public class InventorySellItemCheckRequestHandler implements PacketHandler<FTConnection, CMSGInventorySellItemCheck> {
    private final PlayerPocketService playerPocketService;
    private final PocketService pocketService;
    private final PlayerService playerService;

    private final static byte SUCCESS = 0;
    private final static byte NO_ITEM = -1;
    private final static byte IMPOSSIBLE_ITEM = -2;

    public InventorySellItemCheckRequestHandler() {
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        pocketService = ServiceManager.getInstance().getPocketService();
        playerService = ServiceManager.getInstance().getPlayerService();
    }

    @Override
    public void handle(FTConnection connection, CMSGInventorySellItemCheck packet) {
        FTClient client = connection.getClient();
        if (!client.hasPlayer()) {
            return;
        }

        byte status = SUCCESS;
        int itemPocketId = packet.getItemPocketId();

        FTPlayer player = client.getPlayer();
        long pocketId = player.getPocketId();
        PlayerPocket playerPocket = playerPocketService.getItemAsPocket((long) itemPocketId, pocketId);

        if (playerPocket == null) {
            status = NO_ITEM;
            SMSGInventorySellItemCheck answer = SMSGInventorySellItemCheck.builder().status(status).build();
            connection.sendTCP(answer);
        } else {
            int sellPrice = playerPocketService.getSellPrice(playerPocket);

            SMSGInventorySellItemCheck answer = SMSGInventorySellItemCheck.builder().status(status).build();
            connection.sendTCP(answer);

            List<Integer> itemsCount = IntStream.range(0, playerPocket.getItemCount()).boxed().collect(Collectors.toList());
            StreamUtils.batches(itemsCount, 500)
                    .forEach(itemCount -> {
                        S2CInventorySellItemAnswerPacket inventorySellItemAnswerPacket = new S2CInventorySellItemAnswerPacket((char) itemCount.size(), itemPocketId);
                        connection.sendTCP(inventorySellItemAnswerPacket);
                    });

            playerPocketService.remove(playerPocket.getId());
            pocketService.decrementPocketBelongings(player.getPocketId());
            int newGold = player.getGold() + sellPrice;
            player.syncGold(newGold);
            playerService.updateMoney(player.getPlayer(), sellPrice);

            SMSGSetMoney moneyPacket = SMSGSetMoney.builder()
                    .ap(client.getAp().get())
                    .gold(player.getGold())
                    .build();
            connection.sendTCP(moneyPacket);
        }
    }
}

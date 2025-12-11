package com.jftse.emulator.server.core.handler.inventory;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.shared.packets.inventory.CMSGInventorySell;
import com.jftse.server.core.shared.packets.inventory.SMSGInventorySell;

@PacketId(CMSGInventorySell.PACKET_ID)
public class InventorySellRequestHandler implements PacketHandler<FTConnection, CMSGInventorySell> {
    private final PlayerPocketService playerPocketService;

    private final static byte SUCCESS = 0;
    private final static byte NO_ITEM = -1;
    private final static byte IMPOSSIBLE_ITEM = -2;

    public InventorySellRequestHandler() {
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
    }

    @Override
    public void handle(FTConnection connection, CMSGInventorySell packet) {
        FTClient client = connection.getClient();
        byte status = SUCCESS;
        int itemPocketId = packet.getItemPocketId();

        PlayerPocket playerPocket = playerPocketService.getItemAsPocket((long) itemPocketId, client.getPlayer().getPocket());

        if (playerPocket == null) {
            status = NO_ITEM;

            SMSGInventorySell answer = SMSGInventorySell.builder().status(status).itemPocketId(0).price(0).build();
            connection.sendTCP(answer);
        } else {
            int sellPrice = playerPocketService.getSellPrice(playerPocket);

            SMSGInventorySell answer = SMSGInventorySell.builder()
                    .status(status)
                    .itemPocketId(itemPocketId)
                    .price(sellPrice)
                    .build();
            connection.sendTCP(answer);
        }
    }
}

package com.jftse.emulator.server.core.handler.inventory;

import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.item.EItemUseType;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.service.PocketService;
import com.jftse.server.core.shared.packets.inventory.CMSGInventoryItemTimeExpired;
import com.jftse.server.core.shared.packets.inventory.SMSGInventoryRemoveItem;

import java.util.Date;

@PacketId(CMSGInventoryItemTimeExpired.PACKET_ID)
public class InventoryItemTimeExpiredPacketHandler implements PacketHandler<FTConnection, CMSGInventoryItemTimeExpired> {
    private final PlayerPocketService playerPocketService;
    private final PocketService pocketService;

    public InventoryItemTimeExpiredPacketHandler() {
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        pocketService = ServiceManager.getInstance().getPocketService();
    }

    @Override
    public void handle(FTConnection connection, CMSGInventoryItemTimeExpired packet) {
        FTClient client = connection.getClient();
        if (!client.hasPlayer())
            return;

        FTPlayer player = client.getPlayer();
        long pocketId = player.getPocketId();

        long itemPocketId = packet.getItemPocketId();
        PlayerPocket item = playerPocketService.getItemAsPocket(itemPocketId, pocketId);
        if (item != null && item.getUseType().equals(EItemUseType.TIME.getName())) {
            long timeLeft = (item.getCreated().getTime() * 10000) - (new Date().getTime() * 10000);
            if (timeLeft < 0) {
                playerPocketService.remove(itemPocketId);

                pocketService.decrementPocketBelongings(pocketId);

                SMSGInventoryRemoveItem removeItemPacket = SMSGInventoryRemoveItem.builder().itemPocketId(item.getId().intValue()).build();
                connection.sendTCP(removeItemPacket);
            }
        }
    }
}

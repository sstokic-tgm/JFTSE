package com.jftse.emulator.server.core.handler.game;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.item.EItemUseType;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.inventory.C2SInventoryItemTimeExpiredReqPacket;
import com.jftse.emulator.server.core.packet.packets.inventory.S2CInventoryItemRemoveAnswerPacket;
import com.jftse.emulator.server.core.service.PlayerPocketService;
import com.jftse.emulator.server.core.service.PocketService;
import com.jftse.emulator.server.database.model.pocket.PlayerPocket;
import com.jftse.emulator.server.database.model.pocket.Pocket;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.Date;

public class InventoryItemTimeExpiredPacketHandler extends AbstractHandler {
    private C2SInventoryItemTimeExpiredReqPacket inventoryItemTimeExpiredReqPacket;

    private final PlayerPocketService playerPocketService;
    private final PocketService pocketService;

    public InventoryItemTimeExpiredPacketHandler() {
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        pocketService = ServiceManager.getInstance().getPocketService();
    }

    @Override
    public boolean process(Packet packet) {
        inventoryItemTimeExpiredReqPacket = new C2SInventoryItemTimeExpiredReqPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        long itemPocketId = inventoryItemTimeExpiredReqPacket.getItemPocketId();
        PlayerPocket item = playerPocketService.findById(itemPocketId);
        if (item != null && item.getUseType().equals(EItemUseType.TIME.getName())) {
            long timeLeft = (item.getCreated().getTime() * 10000) - (new Date().getTime() * 10000);
            if (timeLeft < 0) {
                playerPocketService.remove(itemPocketId);

                Pocket pocket = connection.getClient().getActivePlayer().getPocket();
                pocket = pocketService.decrementPocketBelongings(pocket);

                connection.getClient().getActivePlayer().setPocket(pocket);

                S2CInventoryItemRemoveAnswerPacket inventoryItemRemoveAnswerPacket = new S2CInventoryItemRemoveAnswerPacket(inventoryItemTimeExpiredReqPacket.getItemPocketId());
                connection.sendTCP(inventoryItemRemoveAnswerPacket);
            }
        }
    }
}

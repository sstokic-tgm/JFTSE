package com.jftse.emulator.server.core.handler.inventory;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.inventory.C2SInventoryItemTimeExpiredReqPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.item.EItemUseType;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.service.PocketService;
import com.jftse.server.core.shared.packets.inventory.S2CInventoryItemRemoveAnswerPacket;

import java.util.Date;

@PacketOperationIdentifier(PacketOperations.C2SInventoryItemTimeExpiredRequest)
public class InventoryItemTimeExpiredPacketHandler extends AbstractPacketHandler {
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
        FTClient client = (FTClient) connection.getClient();
        if (client == null || client.getPlayer() == null)
            return;

        Player player = client.getPlayer();
        Pocket pocket = player.getPocket();

        long itemPocketId = inventoryItemTimeExpiredReqPacket.getItemPocketId();
        PlayerPocket item = playerPocketService.getItemAsPocket(itemPocketId, pocket);
        if (item != null && item.getUseType().equals(EItemUseType.TIME.getName())) {
            long timeLeft = (item.getCreated().getTime() * 10000) - (new Date().getTime() * 10000);
            if (timeLeft < 0) {
                playerPocketService.remove(itemPocketId);

                pocketService.decrementPocketBelongings(pocket);

                S2CInventoryItemRemoveAnswerPacket inventoryItemRemoveAnswerPacket = new S2CInventoryItemRemoveAnswerPacket(item.getId().intValue());
                connection.sendTCP(inventoryItemRemoveAnswerPacket);
            }
        }
    }
}

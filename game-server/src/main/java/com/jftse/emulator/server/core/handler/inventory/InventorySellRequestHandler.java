package com.jftse.emulator.server.core.handler.inventory;

import com.jftse.emulator.server.core.packets.inventory.C2SInventorySellReqPacket;
import com.jftse.emulator.server.core.packets.inventory.S2CInventorySellAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.PlayerPocketService;

@PacketOperationIdentifier(PacketOperations.C2SInventorySellReq)
public class InventorySellRequestHandler extends AbstractPacketHandler {
    private C2SInventorySellReqPacket inventorySellReqPacket;

    private final PlayerPocketService playerPocketService;

    public InventorySellRequestHandler() {
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
    }

    @Override
    public boolean process(Packet packet) {
        inventorySellReqPacket = new C2SInventorySellReqPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient client = connection.getClient();
        byte status = S2CInventorySellAnswerPacket.SUCCESS;
        int itemPocketId = inventorySellReqPacket.getItemPocketId();

        PlayerPocket playerPocket = playerPocketService.getItemAsPocket((long) itemPocketId, client.getPlayer().getPocket());

        if (playerPocket == null) {
            status = S2CInventorySellAnswerPacket.NO_ITEM;

            S2CInventorySellAnswerPacket inventorySellAnswerPacket = new S2CInventorySellAnswerPacket(status, 0, 0);
            connection.sendTCP(inventorySellAnswerPacket);
        } else {
            int sellPrice = playerPocketService.getSellPrice(playerPocket);

            S2CInventorySellAnswerPacket inventorySellAnswerPacket = new S2CInventorySellAnswerPacket(status, itemPocketId, sellPrice);
            connection.sendTCP(inventorySellAnswerPacket);
        }
    }
}

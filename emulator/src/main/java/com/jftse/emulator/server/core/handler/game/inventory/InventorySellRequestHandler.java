package com.jftse.emulator.server.core.handler.game.inventory;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.inventory.C2SInventorySellReqPacket;
import com.jftse.emulator.server.core.packet.packets.inventory.S2CInventorySellAnswerPacket;
import com.jftse.emulator.server.core.service.PlayerPocketService;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.emulator.server.networking.packet.Packet;

public class InventorySellRequestHandler extends AbstractHandler {
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
        byte status = S2CInventorySellAnswerPacket.SUCCESS;
        int itemPocketId = inventorySellReqPacket.getItemPocketId();

        PlayerPocket playerPocket = playerPocketService.getItemAsPocket((long) itemPocketId, connection.getClient().getPlayer().getPocket());

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

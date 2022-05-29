package com.jftse.emulator.server.core.handler.game.inventory;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.inventory.C2SInventoryWearCardRequestPacket;
import com.jftse.emulator.server.core.packet.packets.inventory.S2CInventoryWearCardAnswerPacket;
import com.jftse.emulator.server.core.service.CardSlotEquipmentService;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;

public class InventoryWearCardPacketHandler extends AbstractHandler {
    private C2SInventoryWearCardRequestPacket inventoryWearCardRequestPacket;

    private final CardSlotEquipmentService cardSlotEquipmentService;

    public InventoryWearCardPacketHandler() {
        cardSlotEquipmentService = ServiceManager.getInstance().getCardSlotEquipmentService();
    }

    @Override
    public boolean process(Packet packet) {
        inventoryWearCardRequestPacket = new C2SInventoryWearCardRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null || connection.getClient().getPlayer() == null)
            return;

        Player player = connection.getClient().getPlayer();

        cardSlotEquipmentService.updateCardSlots(player, inventoryWearCardRequestPacket.getCardSlotList());

        List<Integer> cardSlotList = cardSlotEquipmentService.getEquippedCardSlots(player);
        S2CInventoryWearCardAnswerPacket inventoryWearCardAnswerPacket = new S2CInventoryWearCardAnswerPacket(cardSlotList);
        connection.sendTCP(inventoryWearCardAnswerPacket);
    }
}

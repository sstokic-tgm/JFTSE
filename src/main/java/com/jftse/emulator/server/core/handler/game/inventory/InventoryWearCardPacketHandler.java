package com.jftse.emulator.server.core.handler.game.inventory;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.inventory.C2SInventoryWearCardRequestPacket;
import com.jftse.emulator.server.core.packet.packets.inventory.S2CInventoryWearCardAnswerPacket;
import com.jftse.emulator.server.core.service.CardSlotEquipmentService;
import com.jftse.emulator.server.core.service.PlayerService;
import com.jftse.emulator.server.database.model.player.CardSlotEquipment;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;

public class InventoryWearCardPacketHandler extends AbstractHandler {
    private C2SInventoryWearCardRequestPacket inventoryWearCardRequestPacket;

    private final CardSlotEquipmentService cardSlotEquipmentService;
    private final PlayerService playerService;

    public InventoryWearCardPacketHandler() {
        cardSlotEquipmentService = ServiceManager.getInstance().getCardSlotEquipmentService();
        playerService = ServiceManager.getInstance().getPlayerService();
    }

    @Override
    public boolean process(Packet packet) {
        inventoryWearCardRequestPacket = new C2SInventoryWearCardRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        Player player = playerService.findById(connection.getClient().getActivePlayer().getId());
        CardSlotEquipment cardSlotEquipment = player.getCardSlotEquipment();

        cardSlotEquipmentService.updateCardSlots(cardSlotEquipment, inventoryWearCardRequestPacket.getCardSlotList());
        player.setCardSlotEquipment(cardSlotEquipment);

        player = playerService.save(player);
        connection.getClient().setActivePlayer(player);

        S2CInventoryWearCardAnswerPacket inventoryWearCardAnswerPacket = new S2CInventoryWearCardAnswerPacket(inventoryWearCardRequestPacket.getCardSlotList());
        connection.sendTCP(inventoryWearCardAnswerPacket);
    }
}

package com.jftse.emulator.server.core.handler.game.inventory;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.inventory.C2SInventoryWearQuickReqPacket;
import com.jftse.emulator.server.core.packet.packets.inventory.S2CInventoryWearQuickAnswerPacket;
import com.jftse.emulator.server.core.service.QuickSlotEquipmentService;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;

public class InventoryWearQuickPacketHandler extends AbstractHandler {
    private C2SInventoryWearQuickReqPacket inventoryWearQuickReqPacket;

    private final QuickSlotEquipmentService quickSlotEquipmentService;

    public InventoryWearQuickPacketHandler() {
        quickSlotEquipmentService = ServiceManager.getInstance().getQuickSlotEquipmentService();
    }

    @Override
    public boolean process(Packet packet) {
        inventoryWearQuickReqPacket = new C2SInventoryWearQuickReqPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null || connection.getClient().getPlayer() == null)
            return;

        Player player = connection.getClient().getPlayer();

        quickSlotEquipmentService.updateQuickSlots(player, inventoryWearQuickReqPacket.getQuickSlotList());

        List<Integer> quickSlotList = quickSlotEquipmentService.getEquippedQuickSlots(player);
        S2CInventoryWearQuickAnswerPacket inventoryWearQuickAnswerPacket = new S2CInventoryWearQuickAnswerPacket(quickSlotList);
        connection.sendTCP(inventoryWearQuickAnswerPacket);
    }
}

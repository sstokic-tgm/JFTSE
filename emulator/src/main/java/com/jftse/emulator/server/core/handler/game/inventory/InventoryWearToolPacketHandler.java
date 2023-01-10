package com.jftse.emulator.server.core.handler.game.inventory;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.inventory.C2SInventoryWearToolRequestPacket;
import com.jftse.emulator.server.core.packet.packets.inventory.S2CInventoryWearToolAnswerPacket;
import com.jftse.emulator.server.core.service.ToolSlotEquipmentService;
import com.jftse.entities.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;

public class InventoryWearToolPacketHandler extends AbstractHandler {
    private C2SInventoryWearToolRequestPacket inventoryWearToolRequestPacket;

    private final ToolSlotEquipmentService toolSlotEquipmentService;

    public InventoryWearToolPacketHandler() {
        toolSlotEquipmentService = ServiceManager.getInstance().getToolSlotEquipmentService();
    }

    @Override
    public boolean process(Packet packet) {
        inventoryWearToolRequestPacket = new C2SInventoryWearToolRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null || connection.getClient().getPlayer() == null)
            return;

        Player player = connection.getClient().getPlayer();

        toolSlotEquipmentService.updateToolSlots(player, inventoryWearToolRequestPacket.getToolSlotList());

        List<Integer> toolSlotList = toolSlotEquipmentService.getEquippedToolSlots(player);
        S2CInventoryWearToolAnswerPacket inventoryWearToolAnswerPacket = new S2CInventoryWearToolAnswerPacket(toolSlotList);
        connection.sendTCP(inventoryWearToolAnswerPacket);
    }
}

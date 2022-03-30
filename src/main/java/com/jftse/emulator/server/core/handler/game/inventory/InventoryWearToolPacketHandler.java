package com.jftse.emulator.server.core.handler.game.inventory;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.inventory.C2SInventoryWearToolRequestPacket;
import com.jftse.emulator.server.core.packet.packets.inventory.S2CInventoryWearToolAnswerPacket;
import com.jftse.emulator.server.core.service.ToolSlotEquipmentService;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.database.model.player.ToolSlotEquipment;
import com.jftse.emulator.server.networking.packet.Packet;

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
        Player player = connection.getClient().getPlayer();
        ToolSlotEquipment toolSlotEquipment = player.getToolSlotEquipment();

        toolSlotEquipmentService.updateToolSlots(toolSlotEquipment, inventoryWearToolRequestPacket.getToolSlotList());
        player.setToolSlotEquipment(toolSlotEquipment);
        connection.getClient().savePlayer(player);

        S2CInventoryWearToolAnswerPacket inventoryWearToolAnswerPacket = new S2CInventoryWearToolAnswerPacket(inventoryWearToolRequestPacket.getToolSlotList());
        connection.sendTCP(inventoryWearToolAnswerPacket);
    }
}

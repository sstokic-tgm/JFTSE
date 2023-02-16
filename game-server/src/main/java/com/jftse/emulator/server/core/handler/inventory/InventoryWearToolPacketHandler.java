package com.jftse.emulator.server.core.handler.inventory;

import com.jftse.emulator.server.core.packets.inventory.C2SInventoryWearToolRequestPacket;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryWearToolAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.ToolSlotEquipmentService;

import java.util.List;

@PacketOperationIdentifier(PacketOperations.C2SInventoryWearToolRequest)
public class InventoryWearToolPacketHandler extends AbstractPacketHandler {
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
        FTClient client = (FTClient) connection.getClient();
        if (client == null || client.getPlayer() == null)
            return;

        Player player = client.getPlayer();

        toolSlotEquipmentService.updateToolSlots(player, inventoryWearToolRequestPacket.getToolSlotList());

        List<Integer> toolSlotList = toolSlotEquipmentService.getEquippedToolSlots(player);
        S2CInventoryWearToolAnswerPacket inventoryWearToolAnswerPacket = new S2CInventoryWearToolAnswerPacket(toolSlotList);
        connection.sendTCP(inventoryWearToolAnswerPacket);
    }
}

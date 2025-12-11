package com.jftse.emulator.server.core.handler.inventory;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryWearToolAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.ToolSlotEquipmentService;
import com.jftse.server.core.shared.packets.inventory.CMSGInventoryWearTool;

import java.util.List;

@PacketId(CMSGInventoryWearTool.PACKET_ID)
public class InventoryWearToolPacketHandler implements PacketHandler<FTConnection, CMSGInventoryWearTool> {
    private final ToolSlotEquipmentService toolSlotEquipmentService;

    public InventoryWearToolPacketHandler() {
        toolSlotEquipmentService = ServiceManager.getInstance().getToolSlotEquipmentService();
    }

    @Override
    public void handle(FTConnection connection, CMSGInventoryWearTool packet) {
        FTClient client = connection.getClient();
        if (client == null || client.getPlayer() == null)
            return;

        Player player = client.getPlayer();

        toolSlotEquipmentService.updateToolSlots(player, packet.getToolSlotList());

        List<Integer> toolSlotList = toolSlotEquipmentService.getEquippedToolSlots(player);
        S2CInventoryWearToolAnswerPacket inventoryWearToolAnswerPacket = new S2CInventoryWearToolAnswerPacket(toolSlotList);
        connection.sendTCP(inventoryWearToolAnswerPacket);
    }
}

package com.jftse.emulator.server.core.handler.inventory;

import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryWearToolAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.ToolSlotEquipmentService;
import com.jftse.server.core.shared.packets.inventory.CMSGInventoryWearTool;

@PacketId(CMSGInventoryWearTool.PACKET_ID)
public class InventoryWearToolPacketHandler implements PacketHandler<FTConnection, CMSGInventoryWearTool> {
    private final ToolSlotEquipmentService toolSlotEquipmentService;

    public InventoryWearToolPacketHandler() {
        toolSlotEquipmentService = ServiceManager.getInstance().getToolSlotEquipmentService();
    }

    @Override
    public void handle(FTConnection connection, CMSGInventoryWearTool packet) {
        FTClient client = connection.getClient();
        if (!client.hasPlayer())
            return;

        FTPlayer player = client.getPlayer();
        toolSlotEquipmentService.updateToolSlots(player.getPlayer(), packet.getToolSlotList());
        player.loadToolSlots();

        S2CInventoryWearToolAnswerPacket inventoryWearToolAnswerPacket = new S2CInventoryWearToolAnswerPacket(packet.getToolSlotList());
        connection.sendTCP(inventoryWearToolAnswerPacket);
    }
}

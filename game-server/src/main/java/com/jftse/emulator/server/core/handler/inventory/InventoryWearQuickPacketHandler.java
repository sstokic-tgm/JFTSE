package com.jftse.emulator.server.core.handler.inventory;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryWearQuickAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.QuickSlotEquipmentService;
import com.jftse.server.core.shared.packets.inventory.CMSGInventoryWearQuick;

import java.util.List;

@PacketId(CMSGInventoryWearQuick.PACKET_ID)
public class InventoryWearQuickPacketHandler implements PacketHandler<FTConnection, CMSGInventoryWearQuick> {
    private final QuickSlotEquipmentService quickSlotEquipmentService;

    public InventoryWearQuickPacketHandler() {
        quickSlotEquipmentService = ServiceManager.getInstance().getQuickSlotEquipmentService();
    }

    @Override
    public void handle(FTConnection connection, CMSGInventoryWearQuick packet) {
        FTClient client = connection.getClient();
        if (client == null || client.getPlayer() == null)
            return;

        Player player = client.getPlayer();

        quickSlotEquipmentService.updateQuickSlots(player, packet.getQuickSlotList());

        List<Integer> quickSlotList = quickSlotEquipmentService.getEquippedQuickSlots(player);
        S2CInventoryWearQuickAnswerPacket inventoryWearQuickAnswerPacket = new S2CInventoryWearQuickAnswerPacket(quickSlotList);
        connection.sendTCP(inventoryWearQuickAnswerPacket);
    }
}

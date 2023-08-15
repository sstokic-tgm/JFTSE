package com.jftse.emulator.server.core.handler.inventory;

import com.jftse.emulator.server.core.packets.inventory.C2SInventoryWearQuickReqPacket;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryWearQuickAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.QuickSlotEquipmentService;

import java.util.List;

@PacketOperationIdentifier(PacketOperations.C2SInventoryWearQuickRequest)
public class InventoryWearQuickPacketHandler extends AbstractPacketHandler {
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
        FTClient client = (FTClient) connection.getClient();
        if (client == null || client.getPlayer() == null)
            return;

        Player player = client.getPlayer();

        quickSlotEquipmentService.updateQuickSlots(player, inventoryWearQuickReqPacket.getQuickSlotList());

        List<Integer> quickSlotList = quickSlotEquipmentService.getEquippedQuickSlots(player);
        S2CInventoryWearQuickAnswerPacket inventoryWearQuickAnswerPacket = new S2CInventoryWearQuickAnswerPacket(quickSlotList);
        connection.sendTCP(inventoryWearQuickAnswerPacket);
    }
}

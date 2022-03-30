package com.jftse.emulator.server.core.handler.game.inventory;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.inventory.C2SInventoryWearQuickReqPacket;
import com.jftse.emulator.server.core.packet.packets.inventory.S2CInventoryWearQuickAnswerPacket;
import com.jftse.emulator.server.core.service.QuickSlotEquipmentService;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.database.model.player.QuickSlotEquipment;
import com.jftse.emulator.server.networking.packet.Packet;

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
        Player player = connection.getClient().getPlayer();
        QuickSlotEquipment quickSlotEquipment = player.getQuickSlotEquipment();

        quickSlotEquipmentService.updateQuickSlots(quickSlotEquipment, inventoryWearQuickReqPacket.getQuickSlotList());
        player.setQuickSlotEquipment(quickSlotEquipment);
        connection.getClient().savePlayer(player);

        S2CInventoryWearQuickAnswerPacket inventoryWearQuickAnswerPacket = new S2CInventoryWearQuickAnswerPacket(inventoryWearQuickReqPacket.getQuickSlotList());
        connection.sendTCP(inventoryWearQuickAnswerPacket);
    }
}

package com.jftse.emulator.server.core.handler.game.inventory;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.inventory.C2SInventoryWearSpecialRequestPacket;
import com.jftse.emulator.server.core.packet.packets.inventory.S2CInventoryWearSpecialAnswerPacket;
import com.jftse.emulator.server.core.service.SpecialSlotEquipmentService;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;

public class InventoryWearSpecialPacketHandler extends AbstractHandler {
    private C2SInventoryWearSpecialRequestPacket inventoryWearSpecialRequestPacket;

    private final SpecialSlotEquipmentService specialSlotEquipmentService;

    public InventoryWearSpecialPacketHandler() {
        specialSlotEquipmentService = ServiceManager.getInstance().getSpecialSlotEquipmentService();
    }

    @Override
    public boolean process(Packet packet) {
        inventoryWearSpecialRequestPacket = new C2SInventoryWearSpecialRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null || connection.getClient().getPlayer() == null)
            return;

        Player player = connection.getClient().getPlayer();

        specialSlotEquipmentService.updateSpecialSlots(player, inventoryWearSpecialRequestPacket.getSpecialSlotList());

        List<Integer> specialSlotList = specialSlotEquipmentService.getEquippedSpecialSlots(player);
        S2CInventoryWearSpecialAnswerPacket inventoryWearSpecialAnswerPacket = new S2CInventoryWearSpecialAnswerPacket(specialSlotList);
        connection.sendTCP(inventoryWearSpecialAnswerPacket);
    }
}

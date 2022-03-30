package com.jftse.emulator.server.core.handler.game.inventory;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.inventory.C2SInventoryWearSpecialRequestPacket;
import com.jftse.emulator.server.core.packet.packets.inventory.S2CInventoryWearSpecialAnswerPacket;
import com.jftse.emulator.server.core.service.SpecialSlotEquipmentService;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.database.model.player.SpecialSlotEquipment;
import com.jftse.emulator.server.networking.packet.Packet;

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
        Player player = connection.getClient().getPlayer();
        SpecialSlotEquipment specialSlotEquipment = player.getSpecialSlotEquipment();

        specialSlotEquipmentService.updateSpecialSlots(specialSlotEquipment, inventoryWearSpecialRequestPacket.getSpecialSlotList());
        player.setSpecialSlotEquipment(specialSlotEquipment);
        connection.getClient().savePlayer(player);

        S2CInventoryWearSpecialAnswerPacket inventoryWearSpecialAnswerPacket = new S2CInventoryWearSpecialAnswerPacket(inventoryWearSpecialRequestPacket.getSpecialSlotList());
        connection.sendTCP(inventoryWearSpecialAnswerPacket);
    }
}

package com.jftse.emulator.server.core.handler.inventory;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.inventory.C2SInventoryWearBattlemonReqPacket;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryWearBattlemonAnswerPacket;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.BattlemonSlotEquipmentService;

@PacketOperationIdentifier(PacketOperations.C2SInventoryWearBattlemonRequest)
public class InventoryWearBattlemonPacketHandler extends AbstractPacketHandler {
    private C2SInventoryWearBattlemonReqPacket inventoryWearBattlemonRequestPacket;

    private final BattlemonSlotEquipmentService battlemonSlotEquipmentService;

    public InventoryWearBattlemonPacketHandler() {
        battlemonSlotEquipmentService = ServiceManager.getInstance().getBattlemonSlotEquipmentService();
    }

    @Override
    public boolean process(Packet packet) {
        inventoryWearBattlemonRequestPacket = new C2SInventoryWearBattlemonReqPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        /* Player player = connection.getClient().getPlayer();
        BattlemonSlotEquipment battlemonSlotEquipment = player.getBattlemonSlotEquipment();

        battlemonSlotEquipmentService.updateBattlemonSlots(battlemonSlotEquipment, inventoryWearBattlemonRequestPacket.getBattlemonSlotList());
        player.setBattlemonSlotEquipment(battlemonSlotEquipment);
        connection.getClient().savePlayer(player);
        */

        S2CInventoryWearBattlemonAnswerPacket inventoryWearBattlemonAnswerPacket = new S2CInventoryWearBattlemonAnswerPacket(inventoryWearBattlemonRequestPacket.getBattlemonSlotList());
        connection.sendTCP(inventoryWearBattlemonAnswerPacket);
    }
}

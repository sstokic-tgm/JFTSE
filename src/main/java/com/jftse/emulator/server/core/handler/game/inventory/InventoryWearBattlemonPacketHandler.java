package com.jftse.emulator.server.core.handler.game.inventory;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.inventory.C2SInventoryWearBattlemonReqPacket;
import com.jftse.emulator.server.core.packet.packets.inventory.S2CInventoryWearBattlemonAnswerPacket;
import com.jftse.emulator.server.core.service.BattlemonSlotEquipmentService;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.database.model.player.BattlemonSlotEquipment;
import com.jftse.emulator.server.networking.packet.Packet;

public class InventoryWearBattlemonPacketHandler extends AbstractHandler {
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

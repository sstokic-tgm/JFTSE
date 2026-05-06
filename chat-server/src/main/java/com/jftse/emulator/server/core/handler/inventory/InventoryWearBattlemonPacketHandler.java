package com.jftse.emulator.server.core.handler.inventory;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryWearBattlemonAnswerPacket;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.BattlemonSlotEquipmentService;
import com.jftse.server.core.shared.packets.inventory.CMSGInventoryWearBattlemon;

@PacketId(CMSGInventoryWearBattlemon.PACKET_ID)
public class InventoryWearBattlemonPacketHandler implements PacketHandler<FTConnection, CMSGInventoryWearBattlemon> {
    private final BattlemonSlotEquipmentService battlemonSlotEquipmentService;

    public InventoryWearBattlemonPacketHandler() {
        battlemonSlotEquipmentService = ServiceManager.getInstance().getBattlemonSlotEquipmentService();
    }

    @Override
    public void handle(FTConnection connection, CMSGInventoryWearBattlemon packet) {
        /* Player player = connection.getClient().getPlayer();
        BattlemonSlotEquipment battlemonSlotEquipment = player.getBattlemonSlotEquipment();

        battlemonSlotEquipmentService.updateBattlemonSlots(battlemonSlotEquipment, inventoryWearBattlemonRequestPacket.getBattlemonSlotList());
        player.setBattlemonSlotEquipment(battlemonSlotEquipment);
        connection.getClient().savePlayer(player);
        */

        S2CInventoryWearBattlemonAnswerPacket inventoryWearBattlemonAnswerPacket = new S2CInventoryWearBattlemonAnswerPacket(packet.getBattlemonSlotList());
        connection.sendTCP(inventoryWearBattlemonAnswerPacket);
    }
}

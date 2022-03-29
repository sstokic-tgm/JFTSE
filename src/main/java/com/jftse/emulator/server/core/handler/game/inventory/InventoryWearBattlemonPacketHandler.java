package com.jftse.emulator.server.core.handler.game.inventory;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.inventory.C2SInventoryWearBattlemonReqPacket;
import com.jftse.emulator.server.core.packet.packets.inventory.S2CInventoryWearBattlemonAnswerPacket;
import com.jftse.emulator.server.core.service.PlayerService;
import com.jftse.emulator.server.core.service.BattlemonSlotEquipmentService;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.database.model.player.BattlemonSlotEquipment;
import com.jftse.emulator.server.networking.packet.Packet;

public class InventoryWearBattlemonPacketHandler extends AbstractHandler {
    private C2SInventoryWearBattlemonReqPacket inventoryWearBattlemonRequestPacket;

    private final BattlemonSlotEquipmentService battlemonSlotEquipmentService;
    private final PlayerService playerService;

    public InventoryWearBattlemonPacketHandler() {
        battlemonSlotEquipmentService = ServiceManager.getInstance().getBattlemonSlotEquipmentService();
        playerService = ServiceManager.getInstance().getPlayerService();
    }

    @Override
    public boolean process(Packet packet) {
        inventoryWearBattlemonRequestPacket = new C2SInventoryWearBattlemonReqPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        /* Player player = playerService.findById(connection.getClient().getActivePlayer().getId());
        BattlemonSlotEquipment battlemonSlotEquipment = player.getBattlemonSlotEquipment();

        battlemonSlotEquipmentService.updateBattlemonSlots(battlemonSlotEquipment, inventoryWearBattlemonRequestPacket.getBattlemonSlotList());
        player.setBattlemonSlotEquipment(battlemonSlotEquipment);

        player = playerService.save(player);
        connection.getClient().setActivePlayer(player);
        */

        S2CInventoryWearBattlemonAnswerPacket inventoryWearBattlemonAnswerPacket = new S2CInventoryWearBattlemonAnswerPacket(inventoryWearBattlemonRequestPacket.getBattlemonSlotList());
        connection.sendTCP(inventoryWearBattlemonAnswerPacket);
    }
}

package com.jftse.emulator.server.core.handler.game.inventory;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.matchplay.room.RoomPlayer;
import com.jftse.emulator.server.core.packet.packets.inventory.C2SInventoryWearClothReqPacket;
import com.jftse.emulator.server.core.packet.packets.inventory.S2CInventoryWearClothAnswerPacket;
import com.jftse.emulator.server.core.service.ClothEquipmentService;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.database.model.player.StatusPointsAddedDto;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.Map;

public class InventoryWearClothPacketHandler extends AbstractHandler {
    private C2SInventoryWearClothReqPacket inventoryWearClothReqPacket;

    private final ClothEquipmentService clothEquipmentService;

    public InventoryWearClothPacketHandler() {
        clothEquipmentService = ServiceManager.getInstance().getClothEquipmentService();
    }

    @Override
    public boolean process(Packet packet) {
        inventoryWearClothReqPacket = new C2SInventoryWearClothReqPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null || connection.getClient().getPlayer() == null)
            return;

        Player player = connection.getClient().getPlayer();

        clothEquipmentService.updateCloths(player, inventoryWearClothReqPacket);

        StatusPointsAddedDto statusPointsAddedDto = clothEquipmentService.getStatusPointsFromCloths(player);

        RoomPlayer roomPlayer = connection.getClient().getRoomPlayer();
        if (roomPlayer != null) {
            if (roomPlayer.isFitting()) {
                player = connection.getClient().getPlayer();
                roomPlayer.setClothEquipmentId(player.getClothEquipment().getId());
                roomPlayer.setStatusPointsAddedDto(statusPointsAddedDto);
            }
        }

        Map<String, Integer> equippedCloths = clothEquipmentService.getEquippedCloths(player);
        S2CInventoryWearClothAnswerPacket inventoryWearClothAnswerPacket = new S2CInventoryWearClothAnswerPacket((char) 0, equippedCloths, player, statusPointsAddedDto);
        connection.sendTCP(inventoryWearClothAnswerPacket);
    }
}

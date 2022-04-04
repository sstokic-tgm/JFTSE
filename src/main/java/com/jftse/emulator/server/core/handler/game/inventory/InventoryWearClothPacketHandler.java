package com.jftse.emulator.server.core.handler.game.inventory;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.matchplay.room.RoomPlayer;
import com.jftse.emulator.server.core.packet.packets.inventory.C2SInventoryWearClothReqPacket;
import com.jftse.emulator.server.core.packet.packets.inventory.S2CInventoryWearClothAnswerPacket;
import com.jftse.emulator.server.core.service.ClothEquipmentService;
import com.jftse.emulator.server.database.model.player.ClothEquipment;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.database.model.player.StatusPointsAddedDto;
import com.jftse.emulator.server.networking.packet.Packet;

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
        if (connection.getClient() == null)
            return;

        Player player = connection.getClient().getPlayer();
        ClothEquipment clothEquipment = player.getClothEquipment();

        clothEquipmentService.updateCloths(clothEquipment, inventoryWearClothReqPacket);
        player.setClothEquipment(clothEquipment);
        connection.getClient().savePlayer(player);

        StatusPointsAddedDto statusPointsAddedDto = clothEquipmentService.getStatusPointsFromCloths(player);

        RoomPlayer roomPlayer = connection.getClient().getRoomPlayer();
        if (roomPlayer != null) {
            if (roomPlayer.isFitting()) {
                roomPlayer.setClothEquipmentId(clothEquipment.getId());
                roomPlayer.setStatusPointsAddedDto(statusPointsAddedDto);
            }
        }

        S2CInventoryWearClothAnswerPacket inventoryWearClothAnswerPacket = new S2CInventoryWearClothAnswerPacket((char) 0, inventoryWearClothReqPacket, player, statusPointsAddedDto);
        connection.sendTCP(inventoryWearClothAnswerPacket);
    }
}

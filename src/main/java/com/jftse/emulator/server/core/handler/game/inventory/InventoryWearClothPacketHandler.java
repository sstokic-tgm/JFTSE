package com.jftse.emulator.server.core.handler.game.inventory;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.matchplay.room.Room;
import com.jftse.emulator.server.core.packet.packets.inventory.C2SInventoryWearClothReqPacket;
import com.jftse.emulator.server.core.packet.packets.inventory.S2CInventoryWearClothAnswerPacket;
import com.jftse.emulator.server.core.service.ClothEquipmentService;
import com.jftse.emulator.server.core.service.PlayerService;
import com.jftse.emulator.server.database.model.player.ClothEquipment;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.database.model.player.StatusPointsAddedDto;
import com.jftse.emulator.server.networking.packet.Packet;

public class InventoryWearClothPacketHandler extends AbstractHandler {
    private C2SInventoryWearClothReqPacket inventoryWearClothReqPacket;

    private final ClothEquipmentService clothEquipmentService;
    private final PlayerService playerService;

    public InventoryWearClothPacketHandler() {
        clothEquipmentService = ServiceManager.getInstance().getClothEquipmentService();
        playerService = ServiceManager.getInstance().getPlayerService();
    }

    @Override
    public boolean process(Packet packet) {
        inventoryWearClothReqPacket = new C2SInventoryWearClothReqPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        Player player = playerService.findById(connection.getClient().getActivePlayer().getId());
        ClothEquipment clothEquipment = player.getClothEquipment();

        clothEquipmentService.updateCloths(clothEquipment, inventoryWearClothReqPacket);
        player.setClothEquipment(clothEquipment);

        player = playerService.save(player);
        connection.getClient().setActivePlayer(player);

        StatusPointsAddedDto statusPointsAddedDto = clothEquipmentService.getStatusPointsFromCloths(player);

        Room room = connection.getClient().getActiveRoom();
        if (room != null) {
            room.getRoomPlayerList().forEach(rp -> {
                if (rp.isFitting() && rp.getPlayer().getId().equals(connection.getClient().getActivePlayer().getId())) {
                    rp.setClothEquipment(clothEquipmentService.findClothEquipmentById(clothEquipment.getId()));
                    rp.setStatusPointsAddedDto(statusPointsAddedDto);
                }
            });
        }

        S2CInventoryWearClothAnswerPacket inventoryWearClothAnswerPacket = new S2CInventoryWearClothAnswerPacket((char) 0, inventoryWearClothReqPacket, player, statusPointsAddedDto);
        connection.sendTCP(inventoryWearClothAnswerPacket);
    }
}

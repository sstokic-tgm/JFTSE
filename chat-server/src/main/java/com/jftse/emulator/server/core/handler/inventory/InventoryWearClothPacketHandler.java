package com.jftse.emulator.server.core.handler.inventory;

import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryWearClothAnswerPacket;
import com.jftse.emulator.server.core.service.impl.ClothEquipmentServiceImpl;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.player.StatusPointsAddedDto;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.inventory.CMSGInventoryWearCloth;

import java.util.Map;

@PacketId(CMSGInventoryWearCloth.PACKET_ID)
public class InventoryWearClothPacketHandler implements PacketHandler<FTConnection, CMSGInventoryWearCloth> {
    private final ClothEquipmentServiceImpl clothEquipmentService;

    public InventoryWearClothPacketHandler() {
        clothEquipmentService = ServiceManager.getInstance().getClothEquipmentService();
    }

    @Override
    public void handle(FTConnection connection, CMSGInventoryWearCloth packet) {
        FTClient client = connection.getClient();
        if (client == null || client.getPlayer() == null)
            return;

        Player player = client.getPlayer();

        clothEquipmentService.updateCloths(player, packet);

        StatusPointsAddedDto statusPointsAddedDto = clothEquipmentService.getStatusPointsFromCloths(player);

        RoomPlayer roomPlayer = client.getRoomPlayer();
        if (roomPlayer != null) {
            if (roomPlayer.isFitting()) {
                player = client.getPlayer();
                roomPlayer.setClothEquipmentId(player.getClothEquipment().getId());
                roomPlayer.setStatusPointsAddedDto(statusPointsAddedDto);
            }
        }

        Map<String, Integer> equippedCloths = clothEquipmentService.getEquippedCloths(player);
        S2CInventoryWearClothAnswerPacket inventoryWearClothAnswerPacket = new S2CInventoryWearClothAnswerPacket((char) 0, equippedCloths, player, statusPointsAddedDto);
        connection.sendTCP(inventoryWearClothAnswerPacket);
    }
}

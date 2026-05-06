package com.jftse.emulator.server.core.handler.inventory;

import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryWearClothAnswerPacket;
import com.jftse.emulator.server.core.service.impl.ClothEquipmentServiceImpl;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.PlayerService;
import com.jftse.server.core.shared.packets.inventory.CMSGInventoryWearCloth;

@PacketId(CMSGInventoryWearCloth.PACKET_ID)
public class InventoryWearClothPacketHandler implements PacketHandler<FTConnection, CMSGInventoryWearCloth> {
    private final ClothEquipmentServiceImpl clothEquipmentService;
    private final PlayerService playerService;

    public InventoryWearClothPacketHandler() {
        clothEquipmentService = ServiceManager.getInstance().getClothEquipmentService();
        playerService = ServiceManager.getInstance().getPlayerService();
    }

    @Override
    public void handle(FTConnection connection, CMSGInventoryWearCloth packet) {
        FTClient client = connection.getClient();
        if (!client.hasPlayer())
            return;

        FTPlayer player = client.getPlayer();
        clothEquipmentService.updateCloths(player.getPlayer(), packet);

        Player dbPlayer = playerService.findWithEquipmentById(player.getId());
        player.loadItemParts(dbPlayer);

        S2CInventoryWearClothAnswerPacket inventoryWearClothAnswerPacket = new S2CInventoryWearClothAnswerPacket((char) 0, player);
        connection.sendTCP(inventoryWearClothAnswerPacket);
    }
}

package com.jftse.emulator.server.core.handler.inventory;

import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.inventory.C2SInventoryWearCardRequestPacket;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryWearCardAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.CardSlotEquipmentService;

import java.util.List;

@PacketOperationIdentifier(PacketOperations.C2SInventoryWearCardRequest)
public class InventoryWearCardPacketHandler extends AbstractPacketHandler {
    private C2SInventoryWearCardRequestPacket inventoryWearCardRequestPacket;

    private final CardSlotEquipmentService cardSlotEquipmentService;

    public InventoryWearCardPacketHandler() {
        cardSlotEquipmentService = ServiceManager.getInstance().getCardSlotEquipmentService();
    }

    @Override
    public boolean process(Packet packet) {
        inventoryWearCardRequestPacket = new C2SInventoryWearCardRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
        if (client == null || client.getPlayer() == null)
            return;

        Player player = client.getPlayer();

        cardSlotEquipmentService.updateCardSlots(player, inventoryWearCardRequestPacket.getCardSlotList());

        RoomPlayer roomPlayer = client.getRoomPlayer();
        if (roomPlayer != null) {
            if (roomPlayer.isFitting()) {
                player = client.getPlayer();
                roomPlayer.setCardSlotEquipmentId(player.getCardSlotEquipment().getId());
            }
        }

        List<Integer> cardSlotList = cardSlotEquipmentService.getEquippedCardSlots(player);
        S2CInventoryWearCardAnswerPacket inventoryWearCardAnswerPacket = new S2CInventoryWearCardAnswerPacket(cardSlotList);
        connection.sendTCP(inventoryWearCardAnswerPacket);
    }
}

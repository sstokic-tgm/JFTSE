package com.jftse.emulator.server.core.handler.inventory;

import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryWearCardAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.CardSlotEquipmentService;
import com.jftse.server.core.shared.packets.inventory.CMSGInventoryWearCard;

import java.util.List;

@PacketId(CMSGInventoryWearCard.PACKET_ID)
public class InventoryWearCardPacketHandler implements PacketHandler<FTConnection, CMSGInventoryWearCard> {
    private final CardSlotEquipmentService cardSlotEquipmentService;

    public InventoryWearCardPacketHandler() {
        cardSlotEquipmentService = ServiceManager.getInstance().getCardSlotEquipmentService();
    }

    @Override
    public void handle(FTConnection connection, CMSGInventoryWearCard packet) {
        FTClient client = connection.getClient();
        if (client == null || client.getPlayer() == null)
            return;

        Player player = client.getPlayer();

        cardSlotEquipmentService.updateCardSlots(player, packet.getCardSlotList());

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

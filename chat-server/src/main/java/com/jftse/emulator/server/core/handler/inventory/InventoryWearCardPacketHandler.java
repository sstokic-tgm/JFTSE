package com.jftse.emulator.server.core.handler.inventory;

import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryWearCardAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.CardSlotEquipmentService;
import com.jftse.server.core.shared.packets.inventory.CMSGInventoryWearCard;

@PacketId(CMSGInventoryWearCard.PACKET_ID)
public class InventoryWearCardPacketHandler implements PacketHandler<FTConnection, CMSGInventoryWearCard> {
    private final CardSlotEquipmentService cardSlotEquipmentService;

    public InventoryWearCardPacketHandler() {
        cardSlotEquipmentService = ServiceManager.getInstance().getCardSlotEquipmentService();
    }

    @Override
    public void handle(FTConnection connection, CMSGInventoryWearCard packet) {
        FTClient client = connection.getClient();
        if (!client.hasPlayer())
            return;

        FTPlayer player = client.getPlayer();
        cardSlotEquipmentService.updateCardSlots(player.getPlayer(), packet.getCardSlotList());
        player.loadCardSlots();

        S2CInventoryWearCardAnswerPacket inventoryWearCardAnswerPacket = new S2CInventoryWearCardAnswerPacket(packet.getCardSlotList());
        connection.sendTCP(inventoryWearCardAnswerPacket);
    }
}

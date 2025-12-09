package com.jftse.emulator.server.core.handler.player;

import com.jftse.emulator.server.core.life.item.BaseItem;
import com.jftse.emulator.server.core.life.item.ItemFactory;
import com.jftse.emulator.server.core.rabbit.service.RProducerService;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.shared.packets.player.CMSGUseQuickSlot;
import com.jftse.server.core.shared.rabbit.messages.PacketMessage;
import org.springframework.util.MultiValueMap;

@PacketId(CMSGUseQuickSlot.PACKET_ID)
public class QuickSlotUseRequestHandler implements PacketHandler<FTConnection, CMSGUseQuickSlot> {
    private final RProducerService rProducerService;

    public QuickSlotUseRequestHandler() {
        rProducerService = RProducerService.getInstance();
    }

    @Override
    public void handle(FTConnection connection, CMSGUseQuickSlot quickSlotUseRequestPacket) {
        FTClient ftClient = connection.getClient();
        if (ftClient == null || ftClient.getPlayer() == null)
            return;

        Player player = ftClient.getPlayer();
        Pocket pocket = player.getPocket();
        if (pocket == null)
            return;

        BaseItem baseItem = ItemFactory.getItem(quickSlotUseRequestPacket.getQuickSlotId(), pocket);
        if (baseItem == null)
            return;

        if (baseItem.processPlayer(player)) {
            baseItem.processPocket(pocket);
        }
        sendPackets(baseItem.getPacketsToSend());
    }

    private void sendPackets(MultiValueMap<Long, Packet> packetsToSend) {
        packetsToSend.forEach((playerId, packets) -> {
            for (Packet p : packets) {
                PacketMessage packetMessage = PacketMessage.builder()
                        .packet(p)
                        .receivingPlayerId(playerId)
                        .build();
                rProducerService.send(packetMessage, "game.player.quickSlot chat.player.quickSlot", "ChatServer");
            }
        });
    }
}

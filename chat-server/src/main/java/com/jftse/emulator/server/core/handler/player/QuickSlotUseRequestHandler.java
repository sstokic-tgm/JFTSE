package com.jftse.emulator.server.core.handler.player;

import com.jftse.emulator.server.core.life.item.BaseItem;
import com.jftse.emulator.server.core.life.item.ItemFactory;
import com.jftse.emulator.server.core.packets.player.C2SQuickSlotUseRequestPacket;
import com.jftse.emulator.server.core.rabbit.service.RProducerService;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.shared.rabbit.messages.PacketMessage;
import org.springframework.util.MultiValueMap;

@PacketOperationIdentifier(PacketOperations.C2SQuickSlotUseRequest)
public class QuickSlotUseRequestHandler extends AbstractPacketHandler {
    private C2SQuickSlotUseRequestPacket quickSlotUseRequestPacket;

    private final RProducerService rProducerService;

    public QuickSlotUseRequestHandler() {
        rProducerService = RProducerService.getInstance();
    }

    @Override
    public boolean process(Packet packet) {
        quickSlotUseRequestPacket = new C2SQuickSlotUseRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient ftClient = (FTClient) connection.getClient();
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

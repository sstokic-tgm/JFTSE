package com.jftse.emulator.server.core.handler.item;

import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.life.item.BaseItem;
import com.jftse.emulator.server.core.life.item.ItemFactory;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.item.CMSGPlayerAnnounce;
import com.jftse.server.core.shared.packets.item.SMSGPlayerAnnounce;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.ConcurrentLinkedDeque;

@Log4j2
@PacketId(CMSGPlayerAnnounce.PACKET_ID)
public class PlayerAnnounceHandler implements PacketHandler<FTConnection, CMSGPlayerAnnounce> {
    @Override
    public void handle(FTConnection connection, CMSGPlayerAnnounce packet) {
        FTClient client = connection.getClient();
        if (!client.hasPlayer())
            return;

        FTPlayer player = client.getPlayer();

        BaseItem baseItem = ItemFactory.getItem(packet.getPlayerPocketId(), player.getPocketId());
        if (baseItem == null)
            return;

        if (baseItem.processPlayer(player)) {
            baseItem.processPocket(player.getPocketId());
        }

        byte textSize;
        byte textColor;
        if (baseItem.getItemIndex() == 13) {
            textSize = 0;
            textColor = 0;
        } else {
            textSize = packet.getTextSize();
            textColor = packet.getTextColor();
        }

        SMSGPlayerAnnounce playerAnnounceAnswerPacket = SMSGPlayerAnnounce.builder()
                .playerName(player.getName())
                .textSize(textSize)
                .textColor(textColor)
                .message(packet.getMessage())
                .build();
        final ConcurrentLinkedDeque<FTClient> clients = GameManager.getInstance().getClients();
        clients.stream()
                .filter(c -> c.getConnection() != null)
                .map(FTClient::getConnection)
                .forEach(c -> c.sendTCP(playerAnnounceAnswerPacket));

        baseItem.getPacketsToSend().forEach((playerId, packets) -> {
            final FTConnection connectionByPlayerId = GameManager.getInstance().getConnectionByPlayerId(playerId);
            if (connectionByPlayerId != null) {
                packets.forEach(connectionByPlayerId::sendTCP);
            }
        });
    }
}

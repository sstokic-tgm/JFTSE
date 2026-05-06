package com.jftse.emulator.server.core.handler.enchant;

import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.enchant.CMSGEnchantAnnounce;
import com.jftse.server.core.shared.packets.item.SMSGPlayerAnnounce;

import java.util.concurrent.ConcurrentLinkedDeque;

@PacketId(CMSGEnchantAnnounce.PACKET_ID)
public class EnchantAnnounceHandler implements PacketHandler<FTConnection, CMSGEnchantAnnounce> {
    @Override
    public void handle(FTConnection connection, CMSGEnchantAnnounce packet) {
        FTClient client = connection.getClient();
        if (!client.hasPlayer()) {
            return;
        }
        FTPlayer player = client.getPlayer();

        String playerName = player.getName();
        String message = packet.getMessage();

        int nameEndIndex = message.indexOf('\'', 1);
        String msgPlayerName = message.substring(1, nameEndIndex);
        String msg = message.substring(nameEndIndex + 1);

        if (!playerName.equalsIgnoreCase(msgPlayerName)) {
            return;
        }

        SMSGPlayerAnnounce announcePacket = SMSGPlayerAnnounce.builder()
                .playerName(msgPlayerName)
                .textSize(packet.getTextSize())
                .textColor(packet.getTextColor())
                .message(msg)
                .build();
        final ConcurrentLinkedDeque<FTClient> clients = GameManager.getInstance().getClients();
        clients.stream()
                .filter(c -> c.getConnection() != null)
                .map(FTClient::getConnection)
                .forEach(c -> c.sendTCP(announcePacket));
    }
}

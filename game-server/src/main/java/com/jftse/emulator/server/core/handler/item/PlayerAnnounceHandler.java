package com.jftse.emulator.server.core.handler.item;

import com.jftse.emulator.server.core.life.item.BaseItem;
import com.jftse.emulator.server.core.life.item.ItemFactory;
import com.jftse.emulator.server.core.life.item.special.MegaphoneSpeaker;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packets.item.C2SPlayerAnnouncePacket;
import com.jftse.emulator.server.core.packets.item.S2CPlayerAnnouncePacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.ConcurrentLinkedDeque;

@Log4j2
@PacketOperationIdentifier(PacketOperations.C2SPlayerAnnounce)
public class PlayerAnnounceHandler extends AbstractPacketHandler {
    private C2SPlayerAnnouncePacket playerAnnouncePacket;

    @Override
    public boolean process(Packet packet) {
        playerAnnouncePacket = new C2SPlayerAnnouncePacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
        if (client == null || client.getPlayer() == null)
            return;

        Player player = client.getPlayer();

        Pocket pocket = player.getPocket();
        if (pocket == null)
            return;

        BaseItem baseItem = ItemFactory.getItem(playerAnnouncePacket.getPlayerPocketId(), pocket);
        if (baseItem == null)
            return;

        if (baseItem.processPlayer(player)) {
            baseItem.processPocket(pocket);
        }

        byte textSize;
        byte textColor;
        if (baseItem.getItemIndex() == 13) {
            textSize = 0;
            textColor = 0;
        } else {
            textSize = playerAnnouncePacket.getTextSize();
            textColor = playerAnnouncePacket.getTextColor();
        }

        S2CPlayerAnnouncePacket playerAnnounceAnswerPacket = new S2CPlayerAnnouncePacket(player.getName(), textSize, textColor, playerAnnouncePacket.getMessage());
        final ConcurrentLinkedDeque<FTClient> clients = GameManager.getInstance().getClients();
        clients.stream()
                .filter(c -> c.getConnection() != null && c.getActiveGameSession() == null)
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

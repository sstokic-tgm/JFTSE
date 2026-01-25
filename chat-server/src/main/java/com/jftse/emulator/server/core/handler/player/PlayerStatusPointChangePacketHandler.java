package com.jftse.emulator.server.core.handler.player;

import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.player.S2CPlayerStatusPointChangePacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.PlayerService;
import com.jftse.server.core.shared.packets.player.CMSGChangePlayerStatPoints;

@PacketId(CMSGChangePlayerStatPoints.PACKET_ID)
public class PlayerStatusPointChangePacketHandler implements PacketHandler<FTConnection, CMSGChangePlayerStatPoints> {
    private final PlayerService playerService;

    public PlayerStatusPointChangePacketHandler() {
        playerService = ServiceManager.getInstance().getPlayerService();
    }

    @Override
    public void handle(FTConnection connection, CMSGChangePlayerStatPoints playerStatusPointChangePacket) {
        FTClient ftClient = connection.getClient();
        if (!ftClient.hasPlayer()) {
            return;
        }

        FTPlayer player = ftClient.getPlayer();
        Player dbPlayer = player.getPlayer();

        // we can't change; attributes should be server sided
        if (player.getStatusPoints() == 0) {
            S2CPlayerStatusPointChangePacket playerStatusPointChangeAnswerPacket = new S2CPlayerStatusPointChangePacket(player);
            connection.sendTCP(playerStatusPointChangeAnswerPacket);
        } else if (player.getStatusPoints() > 0 && playerStatusPointChangePacket.getStatusPoints() >= 0) {
            if (playerService.isStatusPointHack(playerStatusPointChangePacket, dbPlayer)) {
                S2CPlayerStatusPointChangePacket playerStatusPointChangeAnswerPacket = new S2CPlayerStatusPointChangePacket(player);
                connection.sendTCP(playerStatusPointChangeAnswerPacket);
            } else {
                player.syncStats(playerStatusPointChangePacket.getStrength(), playerStatusPointChangePacket.getStamina(),
                        playerStatusPointChangePacket.getDexterity(), playerStatusPointChangePacket.getWillpower(),
                        playerStatusPointChangePacket.getStatusPoints());
                playerService.save(dbPlayer);

                S2CPlayerStatusPointChangePacket playerStatusPointChangeAnswerPacket = new S2CPlayerStatusPointChangePacket(player);
                connection.sendTCP(playerStatusPointChangeAnswerPacket);
            }
        }
    }
}

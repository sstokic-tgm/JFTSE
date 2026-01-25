package com.jftse.emulator.server.core.handler.ranking;

import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.ranking.S2CRankingPersonalDataAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.player.PlayerStatistic;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.PlayerService;
import com.jftse.server.core.service.PlayerStatisticService;
import com.jftse.server.core.shared.packets.ranking.CMSGRankingPersonalData;

@PacketId(CMSGRankingPersonalData.PACKET_ID)
public class RankingPersonalDataReqPacketHandler implements PacketHandler<FTConnection, CMSGRankingPersonalData> {
    private final PlayerService playerService;
    private final PlayerStatisticService playerStatisticService;

    public RankingPersonalDataReqPacketHandler() {
        playerService = ServiceManager.getInstance().getPlayerService();
        playerStatisticService = ServiceManager.getInstance().getPlayerStatisticService();
    }

    @Override
    public void handle(FTConnection connection, CMSGRankingPersonalData packet) {
        FTClient ftClient = connection.getClient();
        final byte gameMode = packet.getGameMode();

        if (!ftClient.hasPlayer()) {
            return;
        }

        FTPlayer activePlayer = ftClient.getPlayer();
        Player player = playerService.findByName(packet.getNickname());
        if (player != null) {
            PlayerStatistic playerStatistic = playerStatisticService.findPlayerStatisticById(player.getPlayerStatistic().getId());
            final int ranking = playerService.getPlayerRankingByName(player.getName(), gameMode);

            S2CRankingPersonalDataAnswerPacket rankingPersonalDataAnswerPacket = new S2CRankingPersonalDataAnswerPacket((char) 0, gameMode, player, playerStatistic, ranking);
            connection.sendTCP(rankingPersonalDataAnswerPacket);
        } else {
            PlayerStatistic playerStatistic = playerStatisticService.findPlayerStatisticById(activePlayer.getPlayerStatisticId());
            S2CRankingPersonalDataAnswerPacket rankingPersonalDataAnswerPacket = new S2CRankingPersonalDataAnswerPacket((char) 1, gameMode, activePlayer, playerStatistic, 0);
            connection.sendTCP(rankingPersonalDataAnswerPacket);
        }
    }
}

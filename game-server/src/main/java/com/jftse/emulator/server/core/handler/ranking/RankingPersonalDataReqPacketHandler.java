package com.jftse.emulator.server.core.handler.ranking;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.ranking.S2CRankingPersonalDataAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.PlayerService;
import com.jftse.server.core.shared.packets.ranking.CMSGRankingPersonalData;

@PacketId(CMSGRankingPersonalData.PACKET_ID)
public class RankingPersonalDataReqPacketHandler implements PacketHandler<FTConnection, CMSGRankingPersonalData> {
    private final PlayerService playerService;

    public RankingPersonalDataReqPacketHandler() {
        playerService = ServiceManager.getInstance().getPlayerService();
    }

    @Override
    public void handle(FTConnection connection, CMSGRankingPersonalData packet) {
        FTClient ftClient = connection.getClient();
        if (ftClient == null)
            return;

        final byte gameMode = packet.getGameMode();

        Player activePlayer = ftClient.getPlayer();
        if (activePlayer == null) {
            S2CRankingPersonalDataAnswerPacket rankingPersonalDataAnswerPacket = new S2CRankingPersonalDataAnswerPacket((char) 1, gameMode, new Player(), 0);
            connection.sendTCP(rankingPersonalDataAnswerPacket);
        } else {
            Player player = playerService.findByNameFetched(packet.getNickname());
            if (player != null) {
                final int ranking = playerService.getPlayerRankingByName(player.getName(), gameMode);

                S2CRankingPersonalDataAnswerPacket rankingPersonalDataAnswerPacket = new S2CRankingPersonalDataAnswerPacket((char) 0, gameMode, player, ranking);
                connection.sendTCP(rankingPersonalDataAnswerPacket);
            } else {
                S2CRankingPersonalDataAnswerPacket rankingPersonalDataAnswerPacket = new S2CRankingPersonalDataAnswerPacket((char) 1, gameMode, activePlayer, 0);
                connection.sendTCP(rankingPersonalDataAnswerPacket);
            }
        }
    }
}

package com.jftse.emulator.server.core.handler.game.ranking;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.ranking.C2SRankingPersonalDataRequestPacket;
import com.jftse.emulator.server.core.packet.packets.ranking.S2CRankingPersonalDataAnswerPacket;
import com.jftse.emulator.server.core.service.PlayerService;
import com.jftse.entities.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;

public class RankingPersonalDataReqPacketHandler extends AbstractHandler {
    private C2SRankingPersonalDataRequestPacket rankingPersonalDataRequestPacket;

    private final PlayerService playerService;

    public RankingPersonalDataReqPacketHandler() {
        playerService = ServiceManager.getInstance().getPlayerService();
    }

    @Override
    public boolean process(Packet packet) {
        rankingPersonalDataRequestPacket = new C2SRankingPersonalDataRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null)
            return;

        final byte gameMode = rankingPersonalDataRequestPacket.getGameMode();

        Player activePlayer = connection.getClient().getPlayer();
        if (activePlayer == null) {
            S2CRankingPersonalDataAnswerPacket rankingPersonalDataAnswerPacket = new S2CRankingPersonalDataAnswerPacket((char) 1, gameMode, new Player(), 0);
            connection.sendTCP(rankingPersonalDataAnswerPacket);
        } else {
            Player player = playerService.findByNameFetched(rankingPersonalDataRequestPacket.getNickname());
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

package com.jftse.emulator.server.core.handler.ranking;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.ranking.S2CRankingDataAnswerPacket;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.constants.GameMode;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.PlayerService;
import com.jftse.server.core.shared.packets.ranking.CMSGRankingData;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

@PacketId(CMSGRankingData.PACKET_ID)
public class RankingDataReqPacketHandler implements PacketHandler<FTConnection, CMSGRankingData> {
    private final PlayerService playerService;

    public RankingDataReqPacketHandler() {
        playerService = ServiceManager.getInstance().getPlayerService();
    }

    @Override
    public void handle(FTConnection connection, CMSGRankingData packet) {
        int page = packet.getPage();
        byte gameMode = packet.getGameMode();

        String gameModeRP;
        if (gameMode == GameMode.BASIC)
            gameModeRP = "playerStatistic.basicRP";
        else if (gameMode == GameMode.BATTLE)
            gameModeRP = "playerStatistic.battleRP";
        else
            gameModeRP = "playerStatistic.guardianRP";

        List<Player> allPlayers = playerService.findAllByAlreadyCreatedPageable(PageRequest.of(page == 1 ? 0 : page - 1, 10,
                Sort.by(gameModeRP).descending().and(Sort.by("created"))));
        S2CRankingDataAnswerPacket rankingDataAnswerPacket = new S2CRankingDataAnswerPacket((char) 0, gameMode, page, allPlayers);
        connection.sendTCP(rankingDataAnswerPacket);
    }
}

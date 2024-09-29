package com.jftse.emulator.server.core.handler.ranking;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.ranking.C2SRankingDataRequestPacket;
import com.jftse.emulator.server.core.packets.ranking.S2CRankingDataAnswerPacket;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.constants.GameMode;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.PlayerService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

@PacketOperationIdentifier(PacketOperations.C2SRankingDataReq)
public class RankingDataReqPacketHandler extends AbstractPacketHandler {
    private C2SRankingDataRequestPacket rankingDataRequestPacket;

    private final PlayerService playerService;

    public RankingDataReqPacketHandler() {
        playerService = ServiceManager.getInstance().getPlayerService();
    }

    @Override
    public boolean process(Packet packet) {
        rankingDataRequestPacket = new C2SRankingDataRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        int page = rankingDataRequestPacket.getPage();
        byte gameMode = rankingDataRequestPacket.getGameMode();

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

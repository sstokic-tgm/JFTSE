package com.jftse.emulator.server.core.handler.ranking;

import com.jftse.emulator.server.core.packets.ranking.C2SRankingPersonalDataRequestPacket;
import com.jftse.emulator.server.core.packets.ranking.S2CRankingPersonalDataAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.PlayerService;

@PacketOperationIdentifier(PacketOperations.C2SRankingPersonalDataReq)
public class RankingPersonalDataReqPacketHandler extends AbstractPacketHandler {
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
        FTClient ftClient = connection.getClient();
        if (ftClient == null)
            return;

        final byte gameMode = rankingPersonalDataRequestPacket.getGameMode();

        Player activePlayer = ftClient.getPlayer();
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

package com.jftse.emulator.server.core.handler.challenge;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.challenge.S2CChallengeProgressAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.challenge.ChallengeProgress;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.ChallengeService;
import com.jftse.server.core.shared.packets.challenge.CMSGChallengeProgress;

import java.util.List;

@PacketId(CMSGChallengeProgress.PACKET_ID)
public class ChallengeProgressRequestPacketHandler implements PacketHandler<FTConnection, CMSGChallengeProgress> {
    private final ChallengeService challengeService;

    public ChallengeProgressRequestPacketHandler() {
        challengeService = ServiceManager.getInstance().getChallengeService();
    }

    @Override
    public void handle(FTConnection connection, CMSGChallengeProgress packet) {
        FTClient client = connection.getClient();
        List<ChallengeProgress> challengeProgressList = challengeService.findAllByPlayerIdFetched(client.getPlayer().getId());

        S2CChallengeProgressAnswerPacket challengeProgressAnswerPacket = new S2CChallengeProgressAnswerPacket(challengeProgressList);
        connection.sendTCP(challengeProgressAnswerPacket);
    }
}

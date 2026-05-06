package com.jftse.emulator.server.core.handler.challenge;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.singleplay.challenge.ChallengeBasicGame;
import com.jftse.emulator.server.core.singleplay.challenge.ChallengeBattleGame;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.challenge.Challenge;
import com.jftse.server.core.constants.GameMode;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.ChallengeService;
import com.jftse.server.core.shared.packets.SMSGInitSinglePlayGame;
import com.jftse.server.core.shared.packets.challenge.CMSGRequestChallengeBegin;

@PacketId(CMSGRequestChallengeBegin.PACKET_ID)
public class ChallengeBeginRequestPacketHandler implements PacketHandler<FTConnection, CMSGRequestChallengeBegin> {
    private final ChallengeService challengeService;

    public ChallengeBeginRequestPacketHandler() {
        challengeService = ServiceManager.getInstance().getChallengeService();
    }

    @Override
    public void handle(FTConnection connection, CMSGRequestChallengeBegin challengeBeginRequestPacket) {
        int challengeId = challengeBeginRequestPacket.getChallengeId();

        Challenge currentChallenge = challengeService.findChallengeByChallengeIndex(challengeId);

        FTClient client = connection.getClient();
        if (currentChallenge.getGameMode() == GameMode.BASIC)
            client.setActiveChallengeGame(new ChallengeBasicGame(challengeId));
        else if (currentChallenge.getGameMode() == GameMode.BATTLE)
            client.setActiveChallengeGame(new ChallengeBattleGame(challengeId));

        SMSGInitSinglePlayGame init = SMSGInitSinglePlayGame.builder()
                .result((char) 1)
                .build();
        connection.sendTCP(init);
    }
}

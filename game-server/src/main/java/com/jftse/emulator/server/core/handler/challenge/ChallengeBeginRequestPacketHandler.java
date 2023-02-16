package com.jftse.emulator.server.core.handler.challenge;

import com.jftse.emulator.server.core.packets.challenge.C2SChallengeBeginRequestPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.constants.GameMode;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.emulator.server.core.singleplay.challenge.ChallengeBasicGame;
import com.jftse.emulator.server.core.singleplay.challenge.ChallengeBattleGame;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.challenge.Challenge;
import com.jftse.server.core.service.ChallengeService;

@PacketOperationIdentifier(PacketOperations.C2SChallengeBeginReq)
public class ChallengeBeginRequestPacketHandler extends AbstractPacketHandler {
    private C2SChallengeBeginRequestPacket challengeBeginRequestPacket;

    private final ChallengeService challengeService;

    public ChallengeBeginRequestPacketHandler() {
        challengeService = ServiceManager.getInstance().getChallengeService();
    }

    @Override
    public boolean process(Packet packet) {
        challengeBeginRequestPacket = new C2SChallengeBeginRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        int challengeId = challengeBeginRequestPacket.getChallengeId();

        Challenge currentChallenge = challengeService.findChallengeByChallengeIndex(challengeId);

        FTClient client = (FTClient) connection.getClient();
        if (currentChallenge.getGameMode() == GameMode.BASIC)
            client.setActiveChallengeGame(new ChallengeBasicGame(challengeId));
        else if (currentChallenge.getGameMode() == GameMode.BATTLE)
            client.setActiveChallengeGame(new ChallengeBattleGame(challengeId));

        Packet answer = new Packet(PacketOperations.C2STutorialBegin);
        answer.write((char) 1);
        connection.sendTCP(answer);
    }
}

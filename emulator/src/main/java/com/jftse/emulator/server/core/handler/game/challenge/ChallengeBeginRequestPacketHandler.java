package com.jftse.emulator.server.core.handler.game.challenge;

import com.jftse.emulator.server.core.constants.GameMode;
import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.core.packet.packets.challenge.C2SChallengeBeginRequestPacket;
import com.jftse.emulator.server.core.service.ChallengeService;
import com.jftse.emulator.server.core.singleplay.challenge.ChallengeBasicGame;
import com.jftse.emulator.server.core.singleplay.challenge.ChallengeBattleGame;
import com.jftse.entities.database.model.challenge.Challenge;
import com.jftse.emulator.server.networking.packet.Packet;

public class ChallengeBeginRequestPacketHandler extends AbstractHandler {
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

        if (currentChallenge.getGameMode() == GameMode.BASIC)
            connection.getClient().setActiveChallengeGame(new ChallengeBasicGame(challengeId));
        else if (currentChallenge.getGameMode() == GameMode.BATTLE)
            connection.getClient().setActiveChallengeGame(new ChallengeBattleGame(challengeId));

        Packet answer = new Packet(PacketOperations.C2STutorialBegin.getValueAsChar());
        answer.write((char) 1);
        connection.sendTCP(answer);
    }
}

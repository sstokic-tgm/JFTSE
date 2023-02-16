package com.jftse.emulator.server.core.handler.game.challenge;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.challenge.S2CChallengeProgressAnswerPacket;
import com.jftse.emulator.server.core.service.ChallengeService;
import com.jftse.entities.database.model.challenge.ChallengeProgress;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;

public class ChallengeProgressRequestPacketHandler extends AbstractHandler {
    private final ChallengeService challengeService;

    public ChallengeProgressRequestPacketHandler() {
        challengeService = ServiceManager.getInstance().getChallengeService();
    }

    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        List<ChallengeProgress> challengeProgressList = challengeService.findAllByPlayerIdFetched(connection.getClient().getPlayer().getId());

        S2CChallengeProgressAnswerPacket challengeProgressAnswerPacket = new S2CChallengeProgressAnswerPacket(challengeProgressList);
        connection.sendTCP(challengeProgressAnswerPacket);
    }
}

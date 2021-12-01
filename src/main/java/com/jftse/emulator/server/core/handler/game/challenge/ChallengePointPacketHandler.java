package com.jftse.emulator.server.core.handler.game.challenge;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.challenge.C2SChallengePointPacket;
import com.jftse.emulator.server.core.service.ChallengeService;
import com.jftse.emulator.server.core.singleplay.challenge.ChallengeBasicGame;
import com.jftse.emulator.server.networking.packet.Packet;

public class ChallengePointPacketHandler extends AbstractHandler {
    private C2SChallengePointPacket challengePointPacket;

    private final ChallengeService challengeService;

    public ChallengePointPacketHandler() {
        challengeService = ServiceManager.getInstance().getChallengeService();
    }

    @Override
    public boolean process(Packet packet) {
        challengePointPacket = new C2SChallengePointPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient().getActiveChallengeGame() != null) {
            ((ChallengeBasicGame) connection.getClient().getActiveChallengeGame()).setPoints(challengePointPacket.getPointsPlayer(), challengePointPacket.getPointsNpc());

            if (connection.getClient().getActiveChallengeGame().isFinished()) {
                boolean win = ((ChallengeBasicGame) connection.getClient().getActiveChallengeGame()).getSetsPlayer() == 2;
                challengeService.finishGame(connection, win);

                connection.getClient().setActiveChallengeGame(null);
            }
        }
    }
}

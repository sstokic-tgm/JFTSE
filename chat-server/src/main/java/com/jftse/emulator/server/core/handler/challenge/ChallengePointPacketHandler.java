package com.jftse.emulator.server.core.handler.challenge;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.singleplay.challenge.ChallengeBasicGame;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.ChallengeService;
import com.jftse.server.core.shared.packets.challenge.CMSGChallengePoint;

@PacketId(CMSGChallengePoint.PACKET_ID)
public class ChallengePointPacketHandler implements PacketHandler<FTConnection, CMSGChallengePoint> {
    private final ChallengeService challengeService;

    public ChallengePointPacketHandler() {
        challengeService = ServiceManager.getInstance().getChallengeService();
    }

    @Override
    public void handle(FTConnection connection, CMSGChallengePoint challengePointPacket) {
        FTClient client = connection.getClient();
        if (client.getActiveChallengeGame() instanceof ChallengeBasicGame) {
            ((ChallengeBasicGame) client.getActiveChallengeGame()).setPoints(challengePointPacket.getPointsPlayer(), challengePointPacket.getPointsNpc());

            if (client.getActiveChallengeGame().isFinished()) {
                boolean win = ((ChallengeBasicGame) client.getActiveChallengeGame()).getSetsPlayer() == 2;
                challengeService.finishGame(connection, win);

                client.setActiveChallengeGame(null);
            }
        }
    }
}

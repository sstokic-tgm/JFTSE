package com.jftse.emulator.server.core.handler.challenge;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.challenge.C2SChallengePointPacket;
import com.jftse.emulator.server.core.singleplay.challenge.ChallengeBasicGame;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.ChallengeService;

@PacketOperationIdentifier(PacketOperations.C2SChallengePoint)
public class ChallengePointPacketHandler extends AbstractPacketHandler {
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
        FTClient client = (FTClient) connection.getClient();
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

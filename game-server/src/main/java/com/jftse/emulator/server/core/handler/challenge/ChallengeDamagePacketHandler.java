package com.jftse.emulator.server.core.handler.challenge;

import com.jftse.emulator.server.core.packets.challenge.C2SChallengeDamagePacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.singleplay.challenge.ChallengeBattleGame;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.ChallengeService;

@PacketOperationIdentifier(PacketOperations.C2SChallengeDamage)
public class ChallengeDamagePacketHandler extends AbstractPacketHandler {
    private C2SChallengeDamagePacket challengeDamagePacket;

    private final ChallengeService challengeService;

    public ChallengeDamagePacketHandler() {
        challengeService = ServiceManager.getInstance().getChallengeService();
    }

    @Override
    public boolean process(Packet packet) {
        challengeDamagePacket = new C2SChallengeDamagePacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
        if (client.getActiveChallengeGame() != null) {
            ((ChallengeBattleGame) client.getActiveChallengeGame()).setHp(challengeDamagePacket.getPlayer(), challengeDamagePacket.getDmg());

            if (client.getActiveChallengeGame().isFinished()) {
                boolean win = ((ChallengeBattleGame) client.getActiveChallengeGame()).getPlayerHp() > 0;
                challengeService.finishGame(connection, win);

                client.setActiveChallengeGame(null);
            }
        }
    }
}

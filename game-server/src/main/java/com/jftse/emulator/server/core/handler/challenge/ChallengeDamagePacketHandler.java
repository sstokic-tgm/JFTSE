package com.jftse.emulator.server.core.handler.challenge;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.singleplay.challenge.ChallengeBattleGame;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.ChallengeService;
import com.jftse.server.core.shared.packets.challenge.CMSGChallengeDamage;

@PacketId(CMSGChallengeDamage.PACKET_ID)
public class ChallengeDamagePacketHandler implements PacketHandler<FTConnection, CMSGChallengeDamage> {
    private final ChallengeService challengeService;

    public ChallengeDamagePacketHandler() {
        challengeService = ServiceManager.getInstance().getChallengeService();
    }

    @Override
    public void handle(FTConnection connection, CMSGChallengeDamage challengeDamagePacket) {
        FTClient client = connection.getClient();
        if (client.getActiveChallengeGame() != null) {
            ((ChallengeBattleGame) client.getActiveChallengeGame()).setHp(challengeDamagePacket.getPlayer(), challengeDamagePacket.getDamage());

            if (client.getActiveChallengeGame().isFinished()) {
                boolean win = ((ChallengeBattleGame) client.getActiveChallengeGame()).getPlayerHp() > 0;
                challengeService.finishGame(connection, win);

                client.setActiveChallengeGame(null);
            }
        }
    }
}

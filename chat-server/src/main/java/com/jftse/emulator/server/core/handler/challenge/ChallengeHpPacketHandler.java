package com.jftse.emulator.server.core.handler.challenge;

import com.jftse.emulator.server.core.singleplay.challenge.ChallengeBattleGame;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.challenge.CMSGChallengeInitHp;

@PacketId(CMSGChallengeInitHp.PACKET_ID)
public class ChallengeHpPacketHandler implements PacketHandler<FTConnection, CMSGChallengeInitHp> {
    @Override
    public void handle(FTConnection connection, CMSGChallengeInitHp challengeHpPacket) {
        FTClient client = connection.getClient();
        if (client.getActiveChallengeGame() != null && client.getActiveChallengeGame() instanceof ChallengeBattleGame) {
            ((ChallengeBattleGame) client.getActiveChallengeGame()).setMaxPlayerHp(challengeHpPacket.getPlayerHp());
            ((ChallengeBattleGame) client.getActiveChallengeGame()).setMaxNpcHp(challengeHpPacket.getNpcHp());
        }
    }
}

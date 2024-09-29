package com.jftse.emulator.server.core.handler.challenge;

import com.jftse.emulator.server.core.packets.challenge.C2SChallengeHpPacket;
import com.jftse.emulator.server.core.singleplay.challenge.ChallengeBattleGame;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

@PacketOperationIdentifier(PacketOperations.C2SChallengeHp)
public class ChallengeHpPacketHandler extends AbstractPacketHandler {
    private C2SChallengeHpPacket challengeHpPacket;

    @Override
    public boolean process(Packet packet) {
        challengeHpPacket = new C2SChallengeHpPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
        if (client.getActiveChallengeGame() != null && client.getActiveChallengeGame() instanceof ChallengeBattleGame) {
            ((ChallengeBattleGame) client.getActiveChallengeGame()).setMaxPlayerHp(challengeHpPacket.getPlayerHp());
            ((ChallengeBattleGame) client.getActiveChallengeGame()).setMaxNpcHp(challengeHpPacket.getNpcHp());
        }
    }
}

package com.jftse.emulator.server.core.handler.game.challenge;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.packet.packets.challenge.C2SChallengeHpPacket;
import com.jftse.emulator.server.core.singleplay.challenge.ChallengeBattleGame;
import com.jftse.emulator.server.networking.packet.Packet;

public class ChallengeHpPacketHandler extends AbstractHandler {
    private C2SChallengeHpPacket challengeHpPacket;

    @Override
    public boolean process(Packet packet) {
        challengeHpPacket = new C2SChallengeHpPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient().getActiveChallengeGame() != null && connection.getClient().getActiveChallengeGame() instanceof ChallengeBattleGame) {
            ((ChallengeBattleGame) connection.getClient().getActiveChallengeGame()).setMaxPlayerHp(challengeHpPacket.getPlayerHp());
            ((ChallengeBattleGame) connection.getClient().getActiveChallengeGame()).setMaxNpcHp(challengeHpPacket.getNpcHp());
        }
    }
}

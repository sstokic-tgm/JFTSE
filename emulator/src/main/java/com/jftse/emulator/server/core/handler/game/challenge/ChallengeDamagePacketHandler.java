package com.jftse.emulator.server.core.handler.game.challenge;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.challenge.C2SChallengeDamagePacket;
import com.jftse.emulator.server.core.service.ChallengeService;
import com.jftse.emulator.server.core.singleplay.challenge.ChallengeBattleGame;
import com.jftse.emulator.server.networking.packet.Packet;

public class ChallengeDamagePacketHandler extends AbstractHandler {
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
        if (connection.getClient().getActiveChallengeGame() != null) {
            ((ChallengeBattleGame) connection.getClient().getActiveChallengeGame()).setHp(challengeDamagePacket.getPlayer(), challengeDamagePacket.getDmg());

            if (connection.getClient().getActiveChallengeGame().isFinished()) {
                boolean win = ((ChallengeBattleGame) connection.getClient().getActiveChallengeGame()).getPlayerHp() > 0;
                challengeService.finishGame(connection, win);

                connection.getClient().setActiveChallengeGame(null);
            }
        }
    }
}

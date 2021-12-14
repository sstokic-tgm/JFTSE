package com.jftse.emulator.server.core.handler.relay.spidermine;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.core.matchplay.battle.GuardianBattleState;
import com.jftse.emulator.server.core.matchplay.battle.PlayerBattleState;
import com.jftse.emulator.server.core.matchplay.battle.SkillUse;
import com.jftse.emulator.server.core.matchplay.game.MatchplayBattleGame;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.core.matchplay.room.GameSession;
import com.jftse.emulator.server.core.packet.packets.matchplay.relay.C2CSpiderMinePlacedPacket;
import com.jftse.emulator.server.networking.packet.Packet;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.LinkedBlockingDeque;

@Log4j2
public class SpiderMinePlacedHandler extends AbstractHandler {
    private C2CSpiderMinePlacedPacket spiderMinePlacedPacket;

    @Override
    public boolean process(Packet packet) {
        spiderMinePlacedPacket = new C2CSpiderMinePlacedPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null || connection.getClient().getActiveGameSession() == null)
            return;

        GameSession gameSession = connection.getClient().getActiveGameSession();
        MatchplayGame game = gameSession.getActiveMatchplayGame();

        boolean isBattleGame = game instanceof MatchplayBattleGame;

        ConcurrentLinkedDeque<PlayerBattleState> playerBattleStates = new ConcurrentLinkedDeque<>(isBattleGame ?
                ((MatchplayBattleGame) game).getPlayerBattleStates() :
                ((MatchplayGuardianGame) game).getPlayerBattleStates());
        int playerBattleStatesSize = playerBattleStates.size();
        for (int i = 0; i < playerBattleStatesSize; i++) {
            PlayerBattleState pb = playerBattleStates.poll();
            int skillUseDequeSize = pb.getSkillUseDeque().size();
            setPlacedBySkillUseDeque(skillUseDequeSize, pb.getSkillUseDeque());
        }
        if (game instanceof MatchplayGuardianGame) {
            ConcurrentLinkedDeque<GuardianBattleState> guardianBattleStates = new ConcurrentLinkedDeque<>(((MatchplayGuardianGame) game).getGuardianBattleStates());
            int guardianBattleStatesSize = guardianBattleStates.size();
            for (int i = 0; i < guardianBattleStatesSize; i++) {
                GuardianBattleState gb = guardianBattleStates.poll();
                int skillUseDequeSize = gb.getSkillUseDeque().size();
                setPlacedBySkillUseDeque(skillUseDequeSize, gb.getSkillUseDeque());
            }
        }
    }

    private void setPlacedBySkillUseDeque(int skillUseDequeSize, LinkedBlockingDeque<SkillUse> skillUseDeque) {
        for (int i = 0; i < skillUseDequeSize; i++) {
            try {
                SkillUse current = skillUseDeque.take();

                if (current.isSpiderMine() &&
                        (current.getSpiderMineId() == spiderMinePlacedPacket.getSpiderMineId() ||
                                current.getSpiderMineEffectId() == spiderMinePlacedPacket.getSpiderMineId())) {
                    final boolean notPlaced = !current.getSpiderMineIsPlaced().get();
                    if (!current.getSpiderMineIsPlaced().compareAndSet(notPlaced, true)) {
                        log.debug("spider mine already placed");
                    }
                }
                skillUseDeque.put(current);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}

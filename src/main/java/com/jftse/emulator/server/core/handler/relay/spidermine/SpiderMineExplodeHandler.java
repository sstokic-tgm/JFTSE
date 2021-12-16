package com.jftse.emulator.server.core.handler.relay.spidermine;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ThreadManager;
import com.jftse.emulator.server.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.core.matchplay.battle.GuardianBattleState;
import com.jftse.emulator.server.core.matchplay.battle.PlayerBattleState;
import com.jftse.emulator.server.core.matchplay.battle.SkillUse;
import com.jftse.emulator.server.core.matchplay.game.MatchplayBattleGame;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.core.matchplay.room.GameSession;
import com.jftse.emulator.server.core.packet.packets.matchplay.relay.C2CSpiderMineExplodePacket;
import com.jftse.emulator.server.networking.packet.Packet;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

@Log4j2
public class SpiderMineExplodeHandler extends AbstractHandler {
    private C2CSpiderMineExplodePacket spiderMineExplodePacket;
    private C2CSpiderMineExplodePacket spiderMineExplodePacketToRelay;

    private boolean relayedPacket = false;

    @Override
    public boolean process(Packet packet) {
        spiderMineExplodePacket = new C2CSpiderMineExplodePacket(packet);
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null || connection.getClient().getActiveGameSession() == null)
            return;

        GameSession gameSession = connection.getClient().getActiveGameSession();
        MatchplayGame game = gameSession.getActiveMatchplayGame();

        boolean isBattleGame = game instanceof MatchplayBattleGame;

        if (spiderMineExplodePacket.getTargetPosition() == 4) {
            ConcurrentLinkedDeque<PlayerBattleState> playerBattleStates = new ConcurrentLinkedDeque<>(isBattleGame ?
                    ((MatchplayBattleGame) game).getPlayerBattleStates() :
                    ((MatchplayGuardianGame) game).getPlayerBattleStates());

            int playerBattleStatesSize = playerBattleStates.size();
            for (int i = 0; i < playerBattleStatesSize; i++) {
                PlayerBattleState pb = playerBattleStates.poll();
                int skillUseDequeSize = pb.getSkillUseDeque().size();
                setExplodedBySkillUseDequeScheduled(skillUseDequeSize, pb.getSkillUseDeque());
            }
        }

        ConcurrentLinkedDeque<PlayerBattleState> playerBattleStates = new ConcurrentLinkedDeque<>(isBattleGame ?
                ((MatchplayBattleGame) game).getPlayerBattleStates() :
                ((MatchplayGuardianGame) game).getPlayerBattleStates());

        int playerBattleStatesSize = playerBattleStates.size();
        for (int i = 0; i < playerBattleStatesSize; i++) {
            PlayerBattleState pb = playerBattleStates.poll();
            if (pb.getPosition().get() == spiderMineExplodePacket.getTargetPosition()) {
                int skillUseDequeSize = pb.getSkillUseDeque().size();
                for (int j = 0; j < skillUseDequeSize; j++) {
                    try {
                        SkillUse current = pb.getSkillUseDeque().take();

                        if (current.isSpiderMine() &&
                                (current.getSpiderMineId() == spiderMineExplodePacket.getSpiderMineId() ||
                                        current.getSpiderMineEffectId() == spiderMineExplodePacket.getSpiderMineId())) {
                            ThreadManager.getInstance().schedule(() -> {
                                final boolean notExploded = !current.getSpiderMineIsExploded().get();
                                if (!current.getSpiderMineIsExploded().compareAndSet(notExploded, true)) {
                                    log.debug("spider mine already marked as exploded");
                                } else {
                                    ConcurrentLinkedDeque<PlayerBattleState> otherPlayerBattleStates = new ConcurrentLinkedDeque<>(((MatchplayBattleGame) game).getPlayerBattleStates());
                                    otherPlayerBattleStates.removeIf(opb -> opb.getPosition().get() == spiderMineExplodePacket.getTargetPosition());

                                    int otherPlayerBattleStatesSize = otherPlayerBattleStates.size();
                                    for (int k = 0; k < otherPlayerBattleStatesSize; k++) {
                                        PlayerBattleState opb = otherPlayerBattleStates.poll();
                                        int otherSkillUseDequeSize = opb.getSkillUseDeque().size();
                                        setExplodedBySkillUseDeque(otherSkillUseDequeSize, opb.getSkillUseDeque());
                                    }
                                }
                            }, 3, TimeUnit.SECONDS);
                        }
                        pb.getSkillUseDeque().put(current);

                    } catch (InterruptedException e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
        }

        if (game instanceof MatchplayGuardianGame) {
            ConcurrentLinkedDeque<GuardianBattleState> guardianBattleStates = new ConcurrentLinkedDeque<>(((MatchplayGuardianGame) game).getGuardianBattleStates());
            int guardianBattleStatesSize = guardianBattleStates.size();
            for (int i = 0; i < guardianBattleStatesSize; i++) {
                GuardianBattleState gb = guardianBattleStates.poll();
                int skillUseDequeSize = gb.getSkillUseDeque().size();
                for (int j = 0; j < skillUseDequeSize; j++) {
                    try {
                        SkillUse current = gb.getSkillUseDeque().take();

                        if (current.isSpiderMine() &&
                                (current.getSpiderMineId() == spiderMineExplodePacket.getSpiderMineId() ||
                                        current.getSpiderMineEffectId() == spiderMineExplodePacket.getSpiderMineId())) {
                            ThreadManager.getInstance().schedule(() -> {
                                final boolean notExploded = !current.getSpiderMineIsExploded().get();
                                if (!current.getSpiderMineIsExploded().compareAndSet(notExploded, true)) {
                                    log.debug("spider mine already marked as exploded");
                                } else {
                                    ConcurrentLinkedDeque<GuardianBattleState> otherGuardianBattleStates = new ConcurrentLinkedDeque<>(((MatchplayGuardianGame) game).getGuardianBattleStates());
                                    otherGuardianBattleStates.removeIf(ogb -> ogb.getPosition().get() == spiderMineExplodePacket.getTargetPosition());

                                    int otherGuardianBattleStatesSize = otherGuardianBattleStates.size();
                                    for (int k = 0; k < otherGuardianBattleStatesSize; k++) {
                                        GuardianBattleState ogb = otherGuardianBattleStates.poll();
                                        int otherSkillUseDequeSize = ogb.getSkillUseDeque().size();
                                        setExplodedBySkillUseDeque(otherSkillUseDequeSize, ogb.getSkillUseDeque());
                                    }
                                }
                            }, 3, TimeUnit.SECONDS);
                        }
                        gb.getSkillUseDeque().put(current);

                    } catch (InterruptedException e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
        }

        GameManager.getInstance().sendPacketToAllRelayClientsInSameGameSession(spiderMineExplodePacket, connection);
    }

    private void setExplodedBySkillUseDeque(int skillUseDequeSize, LinkedBlockingDeque<SkillUse> skillUseDeque) {
        for (int i = 0; i < skillUseDequeSize; i++) {
            try {
                SkillUse current = skillUseDeque.take();

                if (current.isSpiderMine() &&
                        (current.getSpiderMineId() == spiderMineExplodePacket.getSpiderMineId() ||
                                current.getSpiderMineEffectId() == spiderMineExplodePacket.getSpiderMineId())) {
                    weakUpdateExploded(current);
                    relaySpiderMinePacket(current);
                }
                skillUseDeque.put(current);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private void setExplodedBySkillUseDequeScheduled(int skillUseDequeSize, LinkedBlockingDeque<SkillUse> skillUseDeque) {
        for (int i = 0; i < skillUseDequeSize; i++) {
            try {
                SkillUse current = skillUseDeque.take();

                if (current.isSpiderMine() &&
                        (current.getSpiderMineId() == spiderMineExplodePacket.getSpiderMineId() ||
                                current.getSpiderMineEffectId() == spiderMineExplodePacket.getSpiderMineId())) {
                    ThreadManager.getInstance().schedule(() -> weakUpdateExploded(current), 3, TimeUnit.SECONDS);
                    relaySpiderMinePacket(current);
                }
                skillUseDeque.put(current);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private void weakUpdateExploded(SkillUse current) {
        final boolean notExploded = !current.getSpiderMineIsExploded().get();
        if (!current.getSpiderMineIsExploded().compareAndSet(notExploded, true)) {
            log.debug("spider mine already marked as exploded");
        }
    }

    private void relaySpiderMinePacket(SkillUse current) {
        short spiderId = (short) (spiderMineExplodePacket.getSpiderMineId() > current.getSpiderMineId() ? current.getSpiderMineEffectId() : current.getSpiderMineId());

        spiderMineExplodePacketToRelay = new C2CSpiderMineExplodePacket(spiderMineExplodePacket.getTargetPosition() != 4 ? current.getTargetPosition() : spiderMineExplodePacket.getTargetPosition(), spiderId);
        if (!this.relayedPacket) {
            GameManager.getInstance().sendPacketToAllRelayClientsInSameGameSession(spiderMineExplodePacketToRelay, connection);
            this.relayedPacket = true;
        }
    }
}

package com.jftse.emulator.server.core.matchplay.guardian;

import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packets.chat.S2CChatRoomAnswerPacket;
import com.jftse.emulator.server.core.task.GuardianAttackTask;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.thread.ThreadManager;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Getter
@Setter
@Log4j2
public class PhaseManager {
    private AtomicReference<BossBattlePhaseable> currentPhase;
    private List<BossBattlePhaseable> phases;
    private Future<?> updateTask;
    private AtomicBoolean isRunning = new AtomicBoolean(false);

    private final Object lock = new Object();

    private PhaseCallback defaultPhaseCallback = new PhaseCallback() {
        @Override
        public void onNextPhase(FTConnection connection) {
            if (hasNextPhase()) {

                BossBattlePhaseable nextPhase = phases.get(phases.indexOf(currentPhase.get()) + 1);

                for (int i = 5; i > 0; i--) {
                    S2CChatRoomAnswerPacket packet = new S2CChatRoomAnswerPacket((byte) 2, "Server", nextPhase.getPhaseName() + " starts in " + i + "...");
                    GameManager.getInstance().sendPacketToAllClientsInSameGameSession(packet, connection);

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        log.error("onPhaseEnd interrupted exception", e);
                    }
                }
                end();

                currentPhase.compareAndSet(currentPhase.get(), nextPhase);
                setPhaseCallback(defaultPhaseCallback);

                start();

                ThreadManager.getInstance().newTask(new GuardianAttackTask(connection));

            } else {
                onPhaseEnd(connection);
            }
        }

        @Override
        public void onPhaseEnd(FTConnection connection) {
            if (!isRunning.compareAndSet(true, false)) {
                return;
            }

            end();
            updateTask.cancel(true);
            updateTask = null;
            ThreadManager.getInstance().newTask(new GuardianAttackTask(connection));
        }
    };

    public PhaseManager(List<BossBattlePhaseable> phases) {
        this.phases = phases;
        currentPhase = new AtomicReference<>(phases.get(0));

        setPhaseCallback(defaultPhaseCallback);
    }

    public void start() {
        synchronized (lock) {
            if (currentPhase.get().hasEnded()) {
                return;
            }
            currentPhase.get().start();
        }
    }

    public void update(FTConnection connection) {
        if (!isRunning.get())
            return;

        synchronized (lock) {
            if (currentPhase.get().hasEnded()) {
                return;
            }
            currentPhase.get().update(connection);
        }
    }

    public void end() {
        synchronized (lock) {
            if (currentPhase.get().hasEnded()) {
                return;
            }
            currentPhase.get().end();
        }
    }

    public boolean hasNextPhase() {
        synchronized (lock) {
            return phases.indexOf(currentPhase.get()) + 1 < phases.size();
        }
    }

    public boolean hasEnded() {
        synchronized (lock) {
            return currentPhase.get().hasEnded();
        }
    }

    public void setPhaseCallback(PhaseCallback phaseCallback) {
        synchronized (lock) {
            currentPhase.get().setPhaseCallback(phaseCallback);
        }
    }

    public long getGuardianAttackLoopTime(AdvancedGuardianState guardian) {
        synchronized (lock) {
            return currentPhase.get().getGuardianAttackLoopTime(guardian);
        }
    }

    public long phaseTime() {
        synchronized (lock) {
            return currentPhase.get().phaseTime();
        }
    }

    public long playTime() {
        synchronized (lock) {
            return currentPhase.get().playTime();
        }
    }

    public synchronized void removePhase(int index) {
        phases.remove(index);
    }

    public synchronized void removePhase(BossBattlePhaseable phase) {
        phases.remove(phase);
    }

    public synchronized void addPhase(int index, BossBattlePhaseable phase) {
        phases.add(index, phase);
    }

    public synchronized void addPhase(BossBattlePhaseable phase) {
        phases.add(phase);
    }

    public String getName() {
        synchronized (lock) {
            return currentPhase.get().getPhaseName();
        }
    }

    public int onHeal(int targetGuardian, int healAmount) {
        synchronized (lock) {
            return currentPhase.get().onHeal(targetGuardian, healAmount);
        }
    }

    public int onDealDamage(int attackingPlayer, int targetGuardian, int damage, boolean hasAttackerDmgBuff, boolean hasTargetDefBuff) {
        synchronized (lock) {
            return currentPhase.get().onDealDamage(attackingPlayer, targetGuardian, damage, hasAttackerDmgBuff, hasTargetDefBuff);
        }
    }

    public int onDealDamageToPlayer(int attackingGuardian, int targetPlayer, int damageAmount, boolean hasAttackerDmgBuff, boolean hasTargetDefBuff) {
        synchronized (lock) {
            return currentPhase.get().onDealDamageToPlayer(attackingGuardian, targetPlayer, damageAmount, hasAttackerDmgBuff, hasTargetDefBuff);
        }
    }

    public int onDealDamageOnBallLoss(int attackerPos, int targetPos, boolean hasAttackerWillBuff) {
        synchronized (lock) {
            return currentPhase.get().onDealDamageOnBallLoss(attackerPos, targetPos, hasAttackerWillBuff);
        }
    }

    public int onDealDamageOnBallLossToPlayer(int attackerPos, int targetPos, boolean hasAttackerWillBuff) {
        synchronized (lock) {
            return currentPhase.get().onDealDamageOnBallLossToPlayer(attackerPos, targetPos, hasAttackerWillBuff);
        }
    }
}

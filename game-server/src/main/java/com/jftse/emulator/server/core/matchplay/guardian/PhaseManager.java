package com.jftse.emulator.server.core.matchplay.guardian;

import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packets.chat.S2CChatRoomAnswerPacket;
import com.jftse.emulator.server.core.task.GuardianAttackTask;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.battle.Skill;
import com.jftse.server.core.thread.ThreadManager;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Getter
@Setter
@Log4j2
public class PhaseManager {
    private AtomicReference<BossBattlePhaseable> currentPhase;
    private List<BossBattlePhaseable> phases;
    private Future<?> updateTask;
    private AtomicBoolean isRunning = new AtomicBoolean(false);

    private AtomicBoolean isChangingPhase = new AtomicBoolean(false);
    private AtomicBoolean isPhaseEnding = new AtomicBoolean(false);

    private final BlockingDeque<Runnable> taskQueue = new LinkedBlockingDeque<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final Lock lock = new ReentrantLock();

    private PhaseCallback defaultPhaseCallback = new PhaseCallback() {
        @Override
        public void onNextPhase(FTConnection connection) {
            log.info("Next phase triggered");
            if (hasNextPhase()) {
                enqueueTask(() -> {
                    log.info("Transitioning to next phase");
                    if (!isChangingPhase.compareAndSet(false, true)) {
                        return;
                    }

                    BossBattlePhaseable nextPhase = phases.get(phases.indexOf(currentPhase.get()) + 1);
                    final String nextPhaseName = nextPhase.getPhaseName();
                    log.info("Next phase name: {}", nextPhaseName);

                    ThreadManager.getInstance().newTask(() -> {
                        for (int i = 5; i > 0; i--) {
                            log.info("Countdown: {}", i);
                            S2CChatRoomAnswerPacket packet = new S2CChatRoomAnswerPacket((byte) 2, "Server", nextPhaseName + " starts in " + i + "...");
                            GameManager.getInstance().sendPacketToAllClientsInSameGameSession(packet, connection);

                            if (i != 1) {
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    log.error("Countdown interrupted exception", e);
                                }
                            }
                        }
                        log.info("Phase ended: {}", currentPhase.get().getPhaseName());
                        currentPhase.get().end();
                        currentPhase.compareAndSet(currentPhase.get(), nextPhase);
                        setPhaseCallback(defaultPhaseCallback);
                        log.info("Starting new phase: {}", nextPhase.getPhaseName());
                        currentPhase.get().start();
                        isChangingPhase.set(false);

                        ThreadManager.getInstance().newTask(new GuardianAttackTask(connection));
                    });
                });
            } else {
                log.info("No next phase available, ending phase");
                onPhaseEnd(connection);
            }
        }

        @Override
        public void onPhaseEnd(FTConnection connection) {
            enqueueTask(() -> {
                if (!isPhaseEnding.compareAndSet(false, true)) {
                    return;
                }

                if (!isRunning.compareAndSet(true, false)) {
                    return;
                }

                currentPhase.get().end();
                try {
                    updateTask.get();
                } catch (InterruptedException e) {
                    log.error("updateTask interrupted exception", e);
                } catch (ExecutionException e) {
                    log.error("updateTask execution exception", e);
                }
                updateTask = null;

                isPhaseEnding.set(false);

                ThreadManager.getInstance().newTask(new GuardianAttackTask(connection));
            });
        }
    };

    public PhaseManager(List<BossBattlePhaseable> phases) {
        this.phases = phases;
        currentPhase = new AtomicReference<>(phases.get(0));

        setPhaseCallback(defaultPhaseCallback);
    }

    public void start() {
        log.info("Starting phase: {}", currentPhase.get().getPhaseName());
        enqueueTask(() -> currentPhase.get().start());
    }

    public void update(FTConnection connection) {
        log.info("Updating phase: {}", currentPhase.get().getPhaseName());
        enqueueTask(() -> currentPhase.get().update(connection));
    }

    public void end() {
        log.info("Ending phase: {}", currentPhase.get().getPhaseName());
        enqueueTask(() -> currentPhase.get().end());
    }

    public boolean hasNextPhase() {
        lock.lock();
        try {
            return phases.indexOf(currentPhase.get()) + 1 < phases.size();
        } finally {
            lock.unlock();
        }
    }

    public boolean hasEnded() {
        var result = executeTask(() -> currentPhase.get().hasEnded());
        return result != null && result;
    }

    public void setPhaseCallback(PhaseCallback phaseCallback) {
        enqueueTask(() -> currentPhase.get().setPhaseCallback(phaseCallback));
    }

    public long getGuardianAttackLoopTime(AdvancedGuardianState guardian) {
        var result = executeTask(() -> currentPhase.get().getGuardianAttackLoopTime(guardian));
        return result == null ? 0 : result;
    }

    public long phaseTime() {
        var result = executeTask(() -> currentPhase.get().phaseTime());
        return result == null ? 0 : result;
    }

    public long playTime() {
        var result = executeTask(() -> currentPhase.get().playTime());
        return result == null ? 0 : result;
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
        lock.lock();
        try {
            return currentPhase.get().getPhaseName();
        } finally {
            lock.unlock();
        }
    }

    public int onHeal(int targetGuardian, int healAmount) {
        var result = executeTask(() -> currentPhase.get().onHeal(targetGuardian, healAmount));
        return result == null ? 0 : result;
    }

    public int onDealDamage(int attackingPlayer, int targetGuardian, int damage, boolean hasAttackerDmgBuff, boolean hasTargetDefBuff, Skill skill) {
        var result = executeTask(() -> currentPhase.get().onDealDamage(attackingPlayer, targetGuardian, damage, hasAttackerDmgBuff, hasTargetDefBuff, skill));
        return result == null ? 0 : result;
    }

    public int onDealDamageToPlayer(int attackingGuardian, int targetPlayer, int damageAmount, boolean hasAttackerDmgBuff, boolean hasTargetDefBuff, Skill skill) {
        var result = executeTask(() -> currentPhase.get().onDealDamageToPlayer(attackingGuardian, targetPlayer, damageAmount, hasAttackerDmgBuff, hasTargetDefBuff, skill));
        return result == null ? 0 : result;
    }

    public int onDealDamageOnBallLoss(int attackerPos, int targetPos, boolean hasAttackerWillBuff) {
        var result = executeTask(() -> currentPhase.get().onDealDamageOnBallLoss(attackerPos, targetPos, hasAttackerWillBuff));
        return result == null ? 0 : result;
    }

    public int onDealDamageOnBallLossToPlayer(int attackerPos, int targetPos, boolean hasAttackerWillBuff) {
        var result = executeTask(() -> currentPhase.get().onDealDamageOnBallLossToPlayer(attackerPos, targetPos, hasAttackerWillBuff));
        return result == null ? 0 : result;
    }

    private void enqueueTask(Runnable task) {
        log.info("Task enqueued: {}", task);
        taskQueue.offer(task);
        executorService.submit(this::executeNextTask);
    }

    private void executeNextTask() {
        log.info("Attempting to acquire lock for task execution");
        lock.lock();
        try {
            Runnable task = taskQueue.poll();
            if (task != null) {
                log.info("Executing task: {}", task);
                task.run();
                log.info("Task completed: {}", task);
            } else {
                log.info("No task to execute");
            }
        } finally {
            log.info("Releasing lock after task execution");
            lock.unlock();
        }
    }

    private <T> T executeTask(Callable<T> task) {
        final FutureTask<T> futureTask = new FutureTask<>(task);
        log.info("Task execution enqueued: {}", futureTask);
        enqueueTask(futureTask);
        try {
            log.info("Waiting for task to complete: {}", futureTask);
            T result = futureTask.get();
            log.info("Task completed with result: {}", result);
            return result;
        } catch (InterruptedException | ExecutionException e) {
            log.error("Task execution exception: {}", task, e);
            return null;
        }
    }
}

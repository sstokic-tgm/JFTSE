package com.jftse.emulator.server.core.matchplay.guardian;

import com.jftse.emulator.common.exception.ValidationException;
import com.jftse.emulator.server.core.constants.PacketEventType;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.matchplay.event.EventHandler;
import com.jftse.emulator.server.core.matchplay.event.RunnableEvent;
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

@Getter
@Setter
@Log4j2
public class PhaseManager {
    private FTConnection hostConnection;

    private AtomicReference<PhaseScript> currentPhase;
    private List<PhaseScript> phases;
    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private AtomicBoolean isUpdating = new AtomicBoolean(false);

    private AtomicBoolean isChangingPhase = new AtomicBoolean(false);
    private AtomicBoolean isPhaseEnding = new AtomicBoolean(false);

    private final EventHandler eventHandler = GameManager.getInstance().getEventHandler();

    private PhaseCallback defaultPhaseCallback = new PhaseCallback() {
        @Override
        public void onNextPhase(FTConnection connection) {
            if (!isChangingPhase.compareAndSet(false, true)) {
                return;
            }

            if (hasNextPhase()) {
                PhaseScript nextPhase = phases.get(phases.indexOf(currentPhase.get()) + 1);
                final String nextPhaseName = nextPhase.getPhaseName();

                for (int i = 5; i >= 1; i--) {
                    S2CChatRoomAnswerPacket packet = new S2CChatRoomAnswerPacket((byte) 2, "Server", nextPhaseName + " starts in " + i + "...");
                    eventHandler.offer(eventHandler.createPacketEvent(connection.getClient(), packet, PacketEventType.DEFAULT, TimeUnit.SECONDS.toMillis(5 - i)));
                }

                RunnableEvent runnableEvent = eventHandler.createRunnableEvent(() -> {
                    currentPhase.get().end();
                    currentPhase.compareAndSet(currentPhase.get(), nextPhase);

                    currentPhase.get().start();
                    isChangingPhase.set(false);

                    ThreadManager.getInstance().newTask(new GuardianAttackTask(connection));
                }, TimeUnit.SECONDS.toMillis(5));
                eventHandler.offer(runnableEvent);
            } else {
                isChangingPhase.set(false);
                onPhaseEnd(connection);
            }
        }

        @Override
        public void onPhaseEnd(FTConnection connection) {
            if (!isPhaseEnding.compareAndSet(false, true)) {
                return;
            }

            if (!isRunning.compareAndSet(true, false)) {
                return;
            }

            currentPhase.get().end();
            isPhaseEnding.set(false);
        }
    };

    public PhaseManager(List<PhaseScript> phases) {
        this.phases = phases;
        currentPhase = new AtomicReference<>(phases.getFirst());
    }

    public void start(FTConnection connection) {
        final PhaseScript current = currentPhase.get();
        if (current != null) {
            this.hostConnection = connection;
            current.start();
            isRunning.set(true);
        }
    }

    public void update(long diff) {
        if (!getIsRunning().get() || getIsChangingPhase().get() || getIsPhaseEnding().get())
            return;

        try {
            if (isUpdating.compareAndSet(false, true)) {
                PhaseUpdateResult result = currentPhase.get().update(hostConnection);
                log.debug("Phase update result: {}", result);

                switch (result) {
                    case NEXT_PHASE -> defaultPhaseCallback.onNextPhase(hostConnection);
                    case END_PHASE -> defaultPhaseCallback.onPhaseEnd(hostConnection);
                    case ERROR -> log.error("Error during phase update for {}", currentPhase.get().getPhaseName());
                    default -> {
                    }
                }

            }
        } catch (Exception e) {
            log.error("Exception during phase update", e);
        } finally {
            isUpdating.set(false);
        }
    }

    public void end() {
        // end() is called from outside origin thread so we must requeue it to event handler
        eventHandler.createRunnableEvent(() -> {
            try {
                validate();
            } catch (ValidationException e) {
                log.error("validate() threw exception {}", e.getMessage(), e);
                return;
            }

            defaultPhaseCallback.onPhaseEnd(hostConnection);
        }, 0);
    }

    public boolean hasNextPhase() {
        return phases.indexOf(currentPhase.get()) + 1 < phases.size();
    }

    public boolean hasEnded() {
        return currentPhase.get().hasEnded();
    }

    public long getGuardianAttackLoopTime(AdvancedGuardianState guardian) {
        try {
            validate();
        } catch (ValidationException e) {
            log.error("validate() threw exception {}", e.getMessage(), e);
            return -1;
        }
        return currentPhase.get().getGuardianAttackLoopTime(guardian);
    }

    public long phaseTime() {
        return currentPhase.get().phaseTime();
    }

    public long playTime() {
        return currentPhase.get().playTime();
    }

    public String getName() {
        return currentPhase.get().getPhaseName();
    }

    public int onHeal(int target, int healAmount, boolean isGuardian) throws ValidationException {
        validate();
        return currentPhase.get().onHeal(target, healAmount, isGuardian);
    }

    public int onDealDamage(int attackingPlayer, int targetGuardian, int damage, boolean hasAttackerDmgBuff, boolean hasTargetDefBuff, Skill skill) throws ValidationException {
        validate();
        return currentPhase.get().onDealDamage(attackingPlayer, targetGuardian, damage, hasAttackerDmgBuff, hasTargetDefBuff, skill);
    }

    public int onDealDamageToPlayer(int attackingGuardian, int targetPlayer, int damageAmount, boolean hasAttackerDmgBuff, boolean hasTargetDefBuff, Skill skill) throws ValidationException {
        validate();
        return currentPhase.get().onDealDamageToPlayer(attackingGuardian, targetPlayer, damageAmount, hasAttackerDmgBuff, hasTargetDefBuff, skill);
    }

    public int onDealDamageOnBallLoss(int attackerPos, int targetPos, boolean hasAttackerWillBuff) throws ValidationException {
        validate();
        return currentPhase.get().onDealDamageOnBallLoss(attackerPos, targetPos, hasAttackerWillBuff);
    }

    public int onDealDamageOnBallLossToPlayer(int attackerPos, int targetPos, boolean hasAttackerWillBuff) throws ValidationException {
        validate();
        return currentPhase.get().onDealDamageOnBallLossToPlayer(attackerPos, targetPos, hasAttackerWillBuff);
    }

    private boolean canExecuteTask() {
        return isRunning.get() && !isChangingPhase.get() && !isPhaseEnding.get() && !isUpdating.get();
    }

    private boolean isUpdating() {
        return isUpdating.get();
    }

    private void validate() throws ValidationException {
        if (isChangingPhase.get() || isPhaseEnding.get()) {
            throw new ValidationException("Cannot execute action while changing phase.");
        }

        if (!isRunning.get()) {
            throw new ValidationException("PhaseManager is not running.");
        }
    }
}

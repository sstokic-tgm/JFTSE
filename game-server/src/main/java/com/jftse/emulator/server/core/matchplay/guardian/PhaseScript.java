package com.jftse.emulator.server.core.matchplay.guardian;

import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.battle.Skill;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.graalvm.polyglot.Context;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Getter
@Setter
@AllArgsConstructor
public class PhaseScript {
    private final BossBattlePhaseable phase;
    private final Context context;
    private final Lock lock = new ReentrantLock();

    public String getPhaseName() {
        lock.lock();
        try {
            try {
                context.enter();
                return phase.getPhaseName();
            } finally {
                context.leave();
            }
        } finally {
            lock.unlock();
        }
    }

    public void start() {
        lock.lock();
        try {
            try {
                context.enter();
                phase.start();
            } finally {
                context.leave();
            }
        } finally {
            lock.unlock();
        }
    }

    public PhaseUpdateResult update(FTConnection connection) {
        lock.lock();
        try {
            try {
                context.enter();
                return phase.update(connection);
            } finally {
                context.leave();
            }
        } finally {
            lock.unlock();
        }
    }

    public void end() {
        lock.lock();
        try {
            try {
                context.enter();
                phase.end();
            } finally {
                context.leave();
            }
        } finally {
            lock.unlock();
        }
    }

    public long phaseTime() {
        lock.lock();
        try {
            try {
                context.enter();
                return phase.phaseTime();
            } finally {
                context.leave();
            }
        } finally {
            lock.unlock();
        }
    }

    public long playTime() {
        lock.lock();
        try {
            try {
                context.enter();
                return phase.playTime();
            } finally {
                context.leave();
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean hasEnded() {
        lock.lock();
        try {
            try {
                context.enter();
                return phase.hasEnded();
            } finally {
                context.leave();
            }
        } finally {
            lock.unlock();
        }
    }

    public long getGuardianAttackLoopTime(AdvancedGuardianState guardian) {
        lock.lock();
        try {
            try {
                context.enter();
                return phase.getGuardianAttackLoopTime(guardian);
            } finally {
                context.leave();
            }
        } finally {
            lock.unlock();
        }
    }

    public int onHeal(int target, int healAmount, boolean isGuardian) {
        lock.lock();
        try {
            try {
                context.enter();
                return phase.onHeal(target, healAmount, isGuardian);
            } finally {
                context.leave();
            }
        } finally {
            lock.unlock();
        }
    }

    public int onDealDamage(int attackingPlayer, int targetGuardian, int damage, boolean hasAttackerDmgBuff, boolean hasTargetDefBuff, Skill skill) {
        lock.lock();
        try {
            try {
                context.enter();
                return phase.onDealDamage(attackingPlayer, targetGuardian, damage, hasAttackerDmgBuff, hasTargetDefBuff, skill);
            } finally {
                context.leave();
            }
        } finally {
            lock.unlock();
        }
    }

    public int onDealDamageToPlayer(int attackingGuardian, int targetPlayer, int damageAmount, boolean hasAttackerDmgBuff, boolean hasTargetDefBuff, Skill skill) {
        lock.lock();
        try {
            try {
                context.enter();
                return phase.onDealDamageToPlayer(attackingGuardian, targetPlayer, damageAmount, hasAttackerDmgBuff, hasTargetDefBuff, skill);
            } finally {
                context.leave();
            }
        } finally {
            lock.unlock();
        }
    }

    public int onDealDamageOnBallLoss(int attackerPos, int targetPos, boolean hasAttackerWillBuff) {
        lock.lock();
        try {
            try {
                context.enter();
                return phase.onDealDamageOnBallLoss(attackerPos, targetPos, hasAttackerWillBuff);
            } finally {
                context.leave();
            }
        } finally {
            lock.unlock();
        }
    }

    public int onDealDamageOnBallLossToPlayer(int attackerPos, int targetPos, boolean hasAttackerWillBuff) {
        lock.lock();
        try {
            try {
                context.enter();
                return phase.onDealDamageOnBallLossToPlayer(attackerPos, targetPos, hasAttackerWillBuff);
            } finally {
                context.leave();
            }
        } finally {
            lock.unlock();
        }
    }
}

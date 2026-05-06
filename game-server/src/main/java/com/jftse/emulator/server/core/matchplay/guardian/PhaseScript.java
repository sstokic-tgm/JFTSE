package com.jftse.emulator.server.core.matchplay.guardian;

import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.battle.Skill;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.graalvm.polyglot.Context;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Getter
@Setter
@AllArgsConstructor
@Log4j2
public class PhaseScript {
    private final BossBattlePhaseable phase;
    private final Context context;
    private final Lock lock = new ReentrantLock();

    public String getPhaseName() {
        lock.lock();
        try {
            boolean entered = false;
            try {
                context.enter();
                entered = true;
                return phase.getPhaseName();
            } finally {
                if (entered) {
                    try {
                        context.leave();
                    } catch (IllegalStateException e) {
                        // we can safely ignore this exception due to the context being already exited
                        logException(e);
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void start() {
        lock.lock();
        try {
            boolean entered = false;
            try {
                context.enter();
                entered = true;
                phase.start();
            } finally {
                if (entered) {
                    try {
                        context.leave();
                    } catch (IllegalStateException e) {
                        // we can safely ignore this exception due to the context being already exited
                        logException(e);
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public PhaseUpdateResult update(FTConnection connection) {
        lock.lock();
        try {
            boolean entered = false;
            try {
                context.enter();
                entered = true;
                return phase.update(connection);
            } finally {
                if (entered) {
                    try {
                        context.leave();
                    } catch (IllegalStateException e) {
                        // we can safely ignore this exception due to the context being already exited
                        logException(e);
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void end() {
        lock.lock();
        try {
            boolean entered = false;
            try {
                context.enter();
                entered = true;
                phase.end();
            } finally {
                if (entered) {
                    try {
                        context.leave();
                    } catch (IllegalStateException e) {
                        // we can safely ignore this exception due to the context being already exited
                        logException(e);
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public long phaseTime() {
        lock.lock();
        try {
            boolean entered = false;
            try {
                context.enter();
                entered = true;
                return phase.phaseTime();
            } finally {
                if (entered) {
                    try {
                        context.leave();
                    } catch (IllegalStateException e) {
                        // we can safely ignore this exception due to the context being already exited
                        logException(e);
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public long playTime() {
        lock.lock();
        try {
            boolean entered = false;
            try {
                context.enter();
                entered = true;
                return phase.playTime();
            } finally {
                if (entered) {
                    try {
                        context.leave();
                    } catch (IllegalStateException e) {
                        // we can safely ignore this exception due to the context being already exited
                        logException(e);
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean hasEnded() {
        lock.lock();
        try {
            boolean entered = false;
            try {
                context.enter();
                entered = true;
                return phase.hasEnded();
            } finally {
                if (entered) {
                    try {
                        context.leave();
                    } catch (IllegalStateException e) {
                        // we can safely ignore this exception due to the context being already exited
                        logException(e);
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public long getGuardianAttackLoopTime(AdvancedGuardianState guardian) {
        lock.lock();
        try {
            boolean entered = false;
            try {
                context.enter();
                entered = true;
                return phase.getGuardianAttackLoopTime(guardian);
            } finally {
                if (entered) {
                    try {
                        context.leave();
                    } catch (IllegalStateException e) {
                        // we can safely ignore this exception due to the context being already exited
                        logException(e);
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public int onHeal(int target, int healAmount, boolean isGuardian) {
        lock.lock();
        try {
            boolean entered = false;
            try {
                context.enter();
                entered = true;
                return phase.onHeal(target, healAmount, isGuardian);
            } finally {
                if (entered) {
                    try {
                        context.leave();
                    } catch (IllegalStateException e) {
                        // we can safely ignore this exception due to the context being already exited
                        logException(e);
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public int onDealDamage(int attackingPlayer, int targetGuardian, int damage, boolean hasAttackerDmgBuff, boolean hasTargetDefBuff, Skill skill) {
        lock.lock();
        try {
            boolean entered = false;
            try {
                context.enter();
                entered = true;
                return phase.onDealDamage(attackingPlayer, targetGuardian, damage, hasAttackerDmgBuff, hasTargetDefBuff, skill);
            } finally {
                if (entered) {
                    try {
                        context.leave();
                    } catch (IllegalStateException e) {
                        // we can safely ignore this exception due to the context being already exited
                        logException(e);
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public int onDealDamageToPlayer(int attackingGuardian, int targetPlayer, int damageAmount, boolean hasAttackerDmgBuff, boolean hasTargetDefBuff, Skill skill) {
        lock.lock();
        try {
            boolean entered = false;
            try {
                context.enter();
                entered = true;
                return phase.onDealDamageToPlayer(attackingGuardian, targetPlayer, damageAmount, hasAttackerDmgBuff, hasTargetDefBuff, skill);
            } finally {
                if (entered) {
                    try {
                        context.leave();
                    } catch (IllegalStateException e) {
                        // we can safely ignore this exception due to the context being already exited
                        logException(e);
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public int onDealDamageOnBallLoss(int attackerPos, int targetPos, boolean hasAttackerWillBuff) {
        lock.lock();
        try {
            boolean entered = false;
            try {
                context.enter();
                entered = true;
                return phase.onDealDamageOnBallLoss(attackerPos, targetPos, hasAttackerWillBuff);
            } finally {
                if (entered) {
                    try {
                        context.leave();
                    } catch (IllegalStateException e) {
                        // we can safely ignore this exception due to the context being already exited
                        logException(e);
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public int onDealDamageOnBallLossToPlayer(int attackerPos, int targetPos, boolean hasAttackerWillBuff) {
        lock.lock();
        try {
            boolean entered = false;
            try {
                context.enter();
                entered = true;
                return phase.onDealDamageOnBallLossToPlayer(attackerPos, targetPos, hasAttackerWillBuff);
            } finally {
                if (entered) {
                    try {
                        context.leave();
                    } catch (IllegalStateException e) {
                        // we can safely ignore this exception due to the context being already exited
                        logException(e);
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    private void logException(IllegalStateException e) {
        log.warn("Failed to leave context: {}", e.getMessage());
    }
}

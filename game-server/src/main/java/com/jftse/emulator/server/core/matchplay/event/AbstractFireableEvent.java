package com.jftse.emulator.server.core.matchplay.event;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstractFireableEvent implements Fireable {
    private final AtomicLong eventFireTime;
    private final AtomicBoolean fired;
    private final AtomicBoolean cancelled;

    @Getter
    @Setter
    private volatile ExecutionMode executionMode = ExecutionMode.ASYNC;

    protected AbstractFireableEvent(long currentTime, long delayMS) {
        this.eventFireTime = new AtomicLong(currentTime + delayMS);
        this.fired = new AtomicBoolean(false);
        this.cancelled = new AtomicBoolean(false);
    }

    public void extendDelay(long additionalDelayMS) {
        eventFireTime.addAndGet(additionalDelayMS);
    }

    @Override
    public boolean shouldFire(long currentTime) {
        return currentTime > eventFireTime.get();
    }

    @Override
    public boolean isFired() {
        return fired.get();
    }

    @Override
    public void fire() {
        if (!isCancelled() && fired.compareAndSet(false, true)) {
            execute();
        }
    }

    protected abstract void execute();

    @Override
    public boolean isCancelled() {
        return cancelled.get();
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled.set(cancelled);
    }
}

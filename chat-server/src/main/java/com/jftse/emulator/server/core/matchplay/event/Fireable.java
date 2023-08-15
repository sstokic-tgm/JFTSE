package com.jftse.emulator.server.core.matchplay.event;

public interface Fireable extends Eventable<Fireable> {
    void fire();
    boolean shouldFire(long currentTime);
    boolean isFired();
    boolean isCancelled();
    void setCancelled(boolean cancelled);
}

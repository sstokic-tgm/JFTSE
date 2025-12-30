package com.jftse.emulator.server.core.matchplay.event;

public interface Fireable {
    void fire();
    boolean shouldFire(long currentTime);
    boolean isFired();
    boolean isCancelled();
    void setCancelled(boolean cancelled);

    ExecutionMode getExecutionMode();
    void setExecutionMode(ExecutionMode mode);
}

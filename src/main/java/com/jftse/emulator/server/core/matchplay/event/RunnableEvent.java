package com.jftse.emulator.server.core.matchplay.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RunnableEvent {
    private Runnable runnable;
    private long runnableTimeStamp;
    private long eventFireTime;
    private boolean fired = false;

    public void fire() {
        fired = true;
        runnable.run();
    }

    public boolean shouldFire(long currentTime) {
        return currentTime - runnableTimeStamp > eventFireTime;
    }
}

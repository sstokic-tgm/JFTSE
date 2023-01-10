package com.jftse.emulator.server.core.matchplay.event;

import com.jftse.server.core.thread.ThreadManager;
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
        ThreadManager.getInstance().newTask(runnable);
    }

    public boolean shouldFire(long currentTime) {
        return currentTime - runnableTimeStamp > eventFireTime;
    }
}

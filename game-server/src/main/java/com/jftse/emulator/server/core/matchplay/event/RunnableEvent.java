package com.jftse.emulator.server.core.matchplay.event;

import com.jftse.server.core.thread.ThreadManager;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RunnableEvent extends AbstractFireableEvent {
    private Runnable runnable;

    @Builder
    public RunnableEvent(Runnable runnable, long currentTime, long delayMS) {
        super(currentTime, delayMS);

        this.runnable = runnable;
    }

    @Override
    protected void execute() {
        if (getExecutionMode() == ExecutionMode.JS_INLINE) {
            runnable.run();
        } else {
            ThreadManager.getInstance().newTask(runnable);
        }
    }
}

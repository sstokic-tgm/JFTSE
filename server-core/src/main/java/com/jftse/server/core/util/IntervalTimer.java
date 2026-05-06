package com.jftse.server.core.util;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IntervalTimer {
    private long interval;
    private long current;

    public IntervalTimer(final long interval) {
        this.interval = interval;
        this.current = 0;
    }

    public IntervalTimer() {
        this.interval = 0;
        this.current = 0;
    }

    public void update(long diff) {
        current += diff;
        if (current < 0) {
            current = 0;
        }
    }

    public boolean passed() {
        return current >= interval;
    }

    public void reset() {
        if (passed()) {
            current %= interval;
        }
    }
}

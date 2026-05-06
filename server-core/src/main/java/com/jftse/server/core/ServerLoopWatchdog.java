package com.jftse.server.core;

import com.jftse.server.core.util.Time;
import lombok.extern.log4j.Log4j2;

/**
 * Watchdog that monitors the {@link ServerLoop} and terminates the JVM if the core loop appears stuck.
 * <p>
 * The watchdog periodically checks the server loop's iteration counter
 * (incremented once per tick by {@link ServerLoop}).
 * If the counter does not change for longer than {@code maxCoreStuckTime}, it logs an error and calls
 * {@link System#exit(int)} with exit code {@code 1}.
 * </p>
 *
 * <h2>Threading</h2>
 * <p>
 * In {@link ServerLoop}, the watchdog thread is started as a <b>daemon</b> thread (it will not prevent JVM shutdown).
 * </p>
 *
 * <h2>What "stuck" means here</h2>
 * <p>
 * This watchdog only detects that the update loop is not iterating (no loop counter increments).
 * It does not detect logical deadlocks inside your update handler if the loop is still ticking.
 * </p>
 */
@Log4j2
public class ServerLoopWatchdog implements Runnable {
    /** Last observed loop counter value. */
    private long serverLoopCounter = 0;
    /** Threshold in milliseconds after which the server is considered stuck. */
    private final long maxCoreStuckTime;
    /** Last time (ms) the loop counter was observed to change. */
    private long lastChangeMsTime;

    /**
     * @param maxCoreStuckTime maximum allowed time (ms) without a loop counter increment
     */
    public ServerLoopWatchdog(long maxCoreStuckTime) {
        this.maxCoreStuckTime = maxCoreStuckTime;
        this.lastChangeMsTime = Time.getMSTime();
    }

    /**
     * Periodically checks the server loop counter (once per second) and exits the process if the loop
     * has not progressed within (@link #maxCoreStuckTime}.
     */
    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            final long curTime = Time.getMSTime();
            final long serverLoopCounter = ServerLoop.getInstance().getTicks();
            if (this.serverLoopCounter != serverLoopCounter) {
                this.lastChangeMsTime = curTime;
                this.serverLoopCounter = serverLoopCounter;
            } else {
                long msTimeDiff = Time.getMSTimeDiff(this.lastChangeMsTime, curTime);
                if (msTimeDiff > this.maxCoreStuckTime) {
                    log.error("Server core seems to be stuck for {} ms! Last loop counter change was at {} ({} seconds ago). Restarting server...",
                            msTimeDiff, this.lastChangeMsTime, msTimeDiff / 1000);
                    System.exit(1);
                }
            }
        }
    }
}

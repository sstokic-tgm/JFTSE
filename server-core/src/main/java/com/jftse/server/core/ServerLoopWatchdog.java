package com.jftse.server.core;

import com.jftse.server.core.util.Time;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ServerLoopWatchdog implements Runnable {
    private long serverLoopCounter = 0;
    private final long maxCoreStuckTime;
    private long lastChangeMsTime;

    public ServerLoopWatchdog(long maxCoreStuckTime) {
        this.maxCoreStuckTime = maxCoreStuckTime;
        this.lastChangeMsTime = Time.getMSTime();
    }

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
            final long serverLoopCounter = ServerLoop.getInstance().getLoopCounter().get();
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

package com.jftse.server.core;

import com.jftse.server.core.shared.ServerConfService;
import com.jftse.server.core.util.Time;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Getter
@Setter
@Log4j2
public class ServerLoop {
    @Getter
    private static ServerLoop instance;

    private final ServerLoopHandler handler;
    private final ServerConfService confService;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread updateThread;
    private Thread watchdogThread;
    private int minUpdateDiff;
    private int maxCoreStuckTime;

    private final AtomicLong loopCounter = new AtomicLong(0);
    private final AtomicLong totalTickDiff = new AtomicLong(0);
    private final AtomicLong totalUpdateTime = new AtomicLong(0);
    private final AtomicLong maxTickDiff = new AtomicLong(0);
    private final AtomicLong maxUpdateTime = new AtomicLong(0);

    public ServerLoop(Optional<ServerLoopHandler> handler, ServerConfService confService) {
        this.handler = handler.orElse(null);
        this.confService = confService;

        instance = this;
    }

    public void start() {
        if (running.compareAndSet(false, true)) {
            this.minUpdateDiff = confService.get("MinServerUpdateTime", Integer.class);
            this.maxCoreStuckTime = confService.get("MaxCoreStuckTime", Integer.class) * 1000; // convert to milliseconds

            updateThread = new Thread(this::updateLoop, "ServerUpdateLoop");
            watchdogThread = new Thread(new ServerLoopWatchdog(this.maxCoreStuckTime), "ServerWatchdog");
            watchdogThread.setDaemon(true);
            updateThread.start();
            watchdogThread.start();
            log.info("Server update loop started.");
        }
    }

    public void stop() {
        if (running.compareAndSet(true, false)) {
            try {
                if (updateThread != null) {
                    updateThread.join(5000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Failed to stop server update loop", e);
            }
            log.info("Server update loop stopped.");
        }
    }

    private void updateLoop() {
        int halfMaxCoreStuckTime = maxCoreStuckTime / 2;
        if (halfMaxCoreStuckTime <= 0) {
            halfMaxCoreStuckTime = Integer.MAX_VALUE;
        }

        long realCurrTime = 0;
        long realPrevTime = Time.getMSTime();

        while (running.get()) {
            loopCounter.incrementAndGet();

            realCurrTime = Time.getMSTime();
            final long diff = Time.getMSTimeDiff(realPrevTime, realCurrTime);

            totalTickDiff.addAndGet(diff);
            maxTickDiff.accumulateAndGet(diff, Math::max);

            if (diff < minUpdateDiff) {
                try {
                    final long sleepTime = minUpdateDiff - diff;
                    if (sleepTime >= halfMaxCoreStuckTime)
                        log.error("Waiting for {} ms with maxCoreStuckTime set to {} ms (diff: {})", sleepTime, maxCoreStuckTime, diff);

                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("Update loop sleep interrupted", e);
                }
                continue;
            }

            final long updateStartTime = Time.getNSTime();
            try {
                handler.update(diff);
            } catch (Exception e) {
                log.error("Exception in server loop update", e);
            }
            final long updateTime = Time.nanoToMillis(Time.getNSTimeDiff(updateStartTime, Time.getNSTime()));

            totalUpdateTime.addAndGet(updateTime);
            maxUpdateTime.accumulateAndGet(updateTime, Math::max);

            realPrevTime = realCurrTime;
        }

        final long loops = loopCounter.get();
        log.info(
                "Uptime: {}, loops: {}, avg tick: {} ms, max tick: {} ms, avg update: {} ms, max update: {} ms",
                Time.getServerUptime(),
                loops,
                totalTickDiff.get() / (double) loops,
                maxTickDiff.get(),
                totalUpdateTime.get() / (double) loops,
                maxUpdateTime.get()
        );
    }
}

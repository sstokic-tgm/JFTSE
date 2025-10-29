package com.jftse.server.core;

import com.jftse.emulator.common.service.ConfigService;
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
    @Getter private static ServerLoop instance;

    private final ServerLoopHandler handler;
    private final ConfigService configService;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread updateThread;
    private Thread watchdogThread;
    private final int minUpdateDiff;
    private final int maxCoreStuckTime;
    private AtomicLong loopCounter = new AtomicLong(0);

    public ServerLoop(Optional<ServerLoopHandler> handler, ConfigService configService) {
        this.handler = handler.orElse(null);
        this.configService = configService;

        this.minUpdateDiff = configService.getValue("server.minServerUpdateTime", 1);
        this.maxCoreStuckTime = configService.getValue("server.maxCoreStuckTime", 60) * 1000;

        instance = this;
    }

    public void start() {
        if (running.compareAndSet(false, true)) {
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
                if (watchdogThread != null) {
                    watchdogThread.join(); // daemon thread, should not block
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

            try {
                handler.update(diff);
            } catch (Exception e) {
                log.error("Exception in server loop update", e);
            }
            realPrevTime = realCurrTime;
        }

        log.info("Uptime: {}, loops: {}, average loop time: {} ms", Time.getServerUptime(), loopCounter, Time.getMSTime() / (float) loopCounter.get());
    }
}

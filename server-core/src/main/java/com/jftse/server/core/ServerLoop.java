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

/**
 * Main fixed-step update loop of the server ("core loop").
 * <p>
 * This component owns the server's update thread and periodically calls a {@link ServerLoopHandler}
 * with a time delta in milliseconds.
 * </p>
 *
 * <h2>Threading model</h2>
 * <ul>
 *     <li><b>Update thread</b> ({@code "ServerUpdateLoop"}): runs {@link #updateLoop()} and calls {@link ServerLoopHandler#update(long)}.</li>
 *     <li><b>Watchdog thread</b> ({@code "ServerWatchdog"}): daemon thread that monitors the loop counter and terminates the JVM if the core appears stuck.</li>
 * </ul>
 *
 * <h2>Update cadence</h2>
 * <p>
 * The loop measures elapsed time using {@link Time#getMSTime()} and passes {@code diff} (milliseconds)
 * to the handler. If {@code diff < MinServerUpdateTime}, the loop sleeps the remaining time to enforce
 * a minimum tick length.
 * </p>
 *
 * <h2>Configuration</h2>
 * <ul>
 *     <li>{@code MinServerUpdateTime} (int, ms): minimum time between updates (sleep is applied if the loop runs faster).</li>
 *     <li>{@code MaxCoreStuckTime} (int, seconds): watchdog threshold; if the loop does not progress for this duration, the JVM is terminated; internally converted to milliseconds.</li>
 * </ul>
 *
 * <h2>Statistics</h2>
 * <p>
 * The loop tracks counters such as max/avg tick time and update handler execution time. These are logged
 * when the loop stops.
 * </p>
 *
 * <h2>Important</h2>
 * <p>
 * This class requires a {@link ServerLoopHandler} bean to be present in the Spring context. If it is missing,
 * the current implementation will set the handler to {@code null} and throw a {@link NullPointerException}
 * when trying to call {@code handler.update(diff)}. This is intentional to fail fast and highlight the misconfiguration.
 * </p>
 *
 * @see ServerLoopHandler
 * @see ServerLoopWatchdog
 * @see ServerConfService
 * @see Time
 */
@Component
@Getter
@Setter
@Log4j2
public class ServerLoop {
    @Getter
    private static ServerLoop instance;

    /**
     * Update callback invoked each tick.
     */
    private final ServerLoopHandler handler;
    /**
     * Configuration access for loop timing parameters.
     */
    private final ServerConfService confService;

    /**
     * Indicates whether the update loop should keep running.
     */
    private final AtomicBoolean running = new AtomicBoolean(false);
    /**
     * Thread running {@link #updateLoop()}.
     */
    private Thread updateThread;
    /**
     * Daemon thread running {@link ServerLoopWatchdog}.
     */
    private Thread watchdogThread;
    /**
     * Minimum tick duration in milliseconds ({@code MinServerUpdateTime}).
     */
    private int minUpdateDiff;
    /**
     * Watchdog threshold in milliseconds ({@code MaxCoreStuckTime} seconds converted to ms).
     */
    private int maxCoreStuckTime;

    /**
     * Number of loop iterations executed since start. Used by the watchdog and for statistics.
     */
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

    /**
     * Starts the update loop and watchdog thread if not already running.
     * <p>
     * Reads timing configuration from {@link ServerConfService} and then starts:
     * <ul>
     *     <li>{@code "ServerUpdateLoop"} (non-daemon)</li>
     *     <li>{@code "ServerWatchdog"} (daemon)</li>
     * </ul>
     * </p>
     */
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

    /**
     * Requests the update loop to stop and waits up to 5 seconds for the update thread to join.
     * <p>
     * The watchdog thread is a daemon and will terminate with the JVM.
     * </p>
     */
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

    /**
     * Update loop body executed by {@code "ServerUpdateLoop"}.
     * <p>
     * Computes elapsed time between iterations (ms) and calls {@link ServerLoopHandler#update(long)}.
     * If the elapsed time is below the configured minimum, the loop sleeps the remaining time.
     * </p>
     */
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

package com.jftse.server.core.thread;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.*;

@Service
@Getter
@Setter
@Log4j2
public class ThreadManager {
    private static ThreadManager instance;

    private ExecutorService virtualThreadExecutor;
    private ScheduledExecutorService virtualScheduledExecutor;

    @PostConstruct
    public void init() {
        instance = this;

        // Use virtual threads for all tasks
        virtualThreadExecutor = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("ThreadManager-VT-", 0L).factory());

        // Use virtual threads for scheduled tasks
        virtualScheduledExecutor = Executors.newScheduledThreadPool(0, Thread.ofVirtual().name("ThreadManager-VT-Scheduled-", 0L).factory());

        log.info("{} initialized", this.getClass().getSimpleName());
    }

    public static ThreadManager getInstance() {
        return instance;
    }

    public void newTask(Runnable runnable) {
        virtualThreadExecutor.execute(runnable);
    }

    public Future<?> submit(Runnable runnable) {
        return virtualThreadExecutor.submit(runnable);
    }

    public Future<?> submit(Callable<?> callable) {
        return virtualThreadExecutor.submit(callable);
    }

    public <T> Future<?> submit(Runnable runnable, T result) {
        return virtualThreadExecutor.submit(runnable, result);
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, long period, TimeUnit timeUnit) {
        return virtualScheduledExecutor.scheduleAtFixedRate(runnable, 0, period, timeUnit);
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, long initialDelay, long period, TimeUnit timeUnit) {
        return virtualScheduledExecutor.scheduleAtFixedRate(runnable, initialDelay, period, timeUnit);
    }

    public ScheduledFuture<?> schedule(Runnable runnable, long delay, TimeUnit timeUnit) {
        return virtualScheduledExecutor.schedule(runnable, delay, timeUnit);
    }

    public ScheduledFuture<?> schedule(Runnable runnable, long delay) {
        return virtualScheduledExecutor.schedule(runnable, delay, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable runnable, long initialDelay, long delay, TimeUnit timeUnit) {
        return virtualScheduledExecutor.scheduleWithFixedDelay(runnable, initialDelay, delay, timeUnit);
    }

    public ExecutorService createSequentialExecutor() {
        return Executors.newSingleThreadExecutor(Thread.ofVirtual().factory());
    }

    @PreDestroy
    public void onExit() {
        virtualThreadExecutor.shutdown();
        virtualScheduledExecutor.shutdown();

        try {
            virtualThreadExecutor.awaitTermination(1, TimeUnit.MINUTES);
            virtualScheduledExecutor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException ie) {
            log.error(ie.getMessage(), ie);
        }
    }
}

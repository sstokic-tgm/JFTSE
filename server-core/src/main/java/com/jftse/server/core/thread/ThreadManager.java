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

    private ThreadPoolExecutor tpe;
    private ScheduledThreadPoolExecutor stpe;

    @PostConstruct
    public void init() {
        instance = this;

        tpe = new ThreadPoolExecutor(6,
                6 * Runtime.getRuntime().availableProcessors(),
                80, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100),
                Executors.defaultThreadFactory(),
                (r, executor) -> {
                    Thread t = new Thread(r);
                    t.start();
                });

        stpe = new ScheduledThreadPoolExecutor(6, r -> {
            Thread t = new Thread(r);
            t.setName("ThreadManager-Worker");
            return t;
        });
        stpe.setKeepAliveTime(5, TimeUnit.MINUTES);
        stpe.allowCoreThreadTimeOut(true);

        log.info(this.getClass().getSimpleName() + " initialized");
    }

    public static ThreadManager getInstance() {
        return instance;
    }

    public void newTask(Runnable runnable) {
        tpe.execute(runnable);
    }

    public Future<?> submit(Runnable runnable) {
        return tpe.submit(runnable);
    }

    public Future<?> submit(Callable<?> callable) {
        return tpe.submit(callable);
    }

    public <T> Future<?> submit(Runnable runnable, T result) {
        return tpe.submit(runnable, result);
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, long period, TimeUnit timeUnit) {
        return stpe.scheduleAtFixedRate(runnable, 0, period, timeUnit);
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, long initialDelay, long period, TimeUnit timeUnit) {
        return stpe.scheduleAtFixedRate(runnable, initialDelay, period, timeUnit);
    }

    public ScheduledFuture<?> schedule(Runnable runnable, long delay, TimeUnit timeUnit) {
        return stpe.schedule(runnable, delay, timeUnit);
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable runnable, long initialDelay, long delay, TimeUnit timeUnit) {
        return stpe.scheduleWithFixedDelay(runnable, initialDelay, delay, timeUnit);
    }

    @PreDestroy
    public void onExit() {
        tpe.shutdown();
        stpe.shutdown();

        try {
            tpe.awaitTermination(1, TimeUnit.MINUTES);
            stpe.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException ie) {
            log.error(ie.getMessage(), ie);
        }
    }
}

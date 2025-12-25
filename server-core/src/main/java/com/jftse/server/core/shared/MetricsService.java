package com.jftse.server.core.shared;

import com.jftse.entities.database.model.ServerType;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

@Service
@RequiredArgsConstructor
@Log4j2
public class MetricsService {
    private final MetricsPersistenceService persistence;

    private static final int FLUSH_THRESHOLD_CALLS = 50;
    private static final int BATCH_SIZE = 100;

    private final AtomicInteger callCounter = new AtomicInteger(0);

    private final ScheduledExecutorService flushExecutor =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "metrics-flusher");
                t.setDaemon(true);
                return t;
            });

    private final ConcurrentHashMap<MetricKey, Accumulator> acc = new ConcurrentHashMap<>();

    @PreDestroy
    private void stopFlusher() {
        try {
            flushSafely();
            flushExecutor.shutdown();
            if (!flushExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                log.warn("Metrics flush executor did not terminate in the allotted time.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            flushExecutor.shutdownNow();
        }
    }

    public void set(String name, long value, ServerType serverType) {
        acc.computeIfAbsent(new MetricKey(name, serverType), k -> new Accumulator()).set(value);
        requestFlushIfNeeded();
    }

    public void increment(String name, long delta, ServerType serverType) {
        acc.computeIfAbsent(new MetricKey(name, serverType), k -> new Accumulator()).add(delta);
        requestFlushIfNeeded();
    }

    public void average(String name, long sample, ServerType serverType) {
        acc.computeIfAbsent(new MetricKey(name, serverType), k -> new Accumulator()).sample(sample);
        requestFlushIfNeeded();
    }

    private void requestFlushIfNeeded() {
        if (callCounter.incrementAndGet() % FLUSH_THRESHOLD_CALLS == 0) {
            flushExecutor.execute(this::flushSafely);
        }
    }

    private void flushSafely() {
        try {
            flush();
        } catch (Throwable t) {
            log.error("Error flushing metrics", t);
        }
    }

    private void flush() {
        List<Map.Entry<MetricKey, AccumulatorSnapshot>> batch = drainSnapshots();
        if (batch.isEmpty()) return;

        persistence.flushBatch(batch);

        if (!acc.isEmpty()) {
            flushExecutor.execute(this::flushSafely);
        }
    }

    private List<Map.Entry<MetricKey, AccumulatorSnapshot>> drainSnapshots() {
        List<Map.Entry<MetricKey, AccumulatorSnapshot>> out = new ArrayList<>(Math.min(BATCH_SIZE, acc.size()));

        for (Map.Entry<MetricKey, Accumulator> e : acc.entrySet()) {
            if (out.size() >= BATCH_SIZE) break;

            AccumulatorSnapshot snap = e.getValue().snapshotAndReset();
            if (snap.isNoop()) {
                acc.remove(e.getKey(), e.getValue());
                continue;
            }
            out.add(Map.entry(e.getKey(), snap));
        }
        return out;
    }

    public record MetricKey(String name, ServerType serverType) {
    }

    private static final class Accumulator {
        final LongAdder delta = new LongAdder();

        final LongAdder sampleSum = new LongAdder();
        final LongAdder sampleCount = new LongAdder();

        volatile boolean hasSet = false;
        volatile long setValue = 0;

        void set(long value) {
            hasSet = true;
            setValue = value;
            delta.reset();
            sampleSum.reset();
            sampleCount.reset();
        }

        void add(long d) {
            if (!hasSet) delta.add(d);
            else setValue += d;
        }

        void sample(long v) {
            if (hasSet) return;
            sampleSum.add(v);
            sampleCount.increment();
        }

        AccumulatorSnapshot snapshotAndReset() {
            boolean s = hasSet;
            long sv = setValue;

            long d = delta.sumThenReset();
            long ss = sampleSum.sumThenReset();
            long sc = sampleCount.sumThenReset();

            hasSet = false;
            setValue = 0;

            return new AccumulatorSnapshot(s, sv, d, ss, sc);
        }
    }

    public record AccumulatorSnapshot(boolean hasSet, long setValue, long delta, long sampleSum, long sampleCount) {
        boolean isNoop() {
            return !hasSet && delta == 0 && sampleCount == 0;
        }
    }
}

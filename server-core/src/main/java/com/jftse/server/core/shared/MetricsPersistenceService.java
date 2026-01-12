package com.jftse.server.core.shared;

import com.jftse.entities.database.model.Metric;
import com.jftse.entities.database.model.ServerType;
import com.jftse.server.core.util.Time;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MetricsPersistenceService {
    @PersistenceContext
    private EntityManager em;

    private static final double EMA_ALPHA = 0.20;

    @Transactional
    public void flushBatch(List<Map.Entry<MetricsService.MetricKey, MetricsService.AccumulatorSnapshot>> batch) {
        long ts = Time.getNSTime();

        Map<ServerType, List<String>> namesByType = new HashMap<>();
        for (var e : batch) {
            namesByType.computeIfAbsent(e.getKey().serverType(), k -> new ArrayList<>()).add(e.getKey().name());
        }

        // load existing metrics in bulk
        Map<String, Metric> existing = new HashMap<>();
        for (var entry : namesByType.entrySet()) {
            ServerType st = entry.getKey();
            List<String> names = entry.getValue();

            List<Metric> found = em.createQuery(
                            "SELECT m FROM Metric m WHERE m.serverType = :st AND m.name IN :names", Metric.class)
                    .setParameter("st", st)
                    .setParameter("names", names)
                    .getResultList();

            for (Metric m : found) {
                existing.put(st.name() + "|" + m.getName(), m);
            }
        }

        for (var e : batch) {
            var key = e.getKey();
            var snap = e.getValue();

            String mapKey = key.serverType().name() + "|" + key.name();
            Metric m = existing.get(mapKey);

            long newValue = updateValue(m, snap);

            if (m == null) {
                m = new Metric();
                m.setName(key.name());
                m.setServerType(key.serverType());
                em.persist(m);
                existing.put(mapKey, m);
            }

            m.setValue(newValue);
            m.setTimestamp(ts);
        }

        em.flush();
    }

    private long updateValue(Metric m, MetricsService.AccumulatorSnapshot snap) {
        long base = (m == null || m.getValue() == null) ? 0L : m.getValue();
        long newValue = base;

        if (snap.hasSet()) {
            newValue = snap.setValue();
        } else {
            if (snap.delta() != 0) {
                newValue += snap.delta();
            }

            if (snap.sampleCount() > 0) {
                long sampleAvg = snap.sampleSum() / snap.sampleCount();

                if (m == null || m.getValue() == null) {
                    newValue = sampleAvg;
                } else {
                    double ema = base + EMA_ALPHA * (sampleAvg - base);
                    newValue = Math.round(ema);
                }
            }
        }
        return newValue;
    }

    @Transactional(readOnly = true)
    public Metric getMetric(String name, ServerType serverType) {
        return em.createQuery("SELECT m FROM Metric m WHERE m.name = :name AND m.serverType = :serverType", Metric.class)
                .setParameter("name", name)
                .setParameter("serverType", serverType)
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }
}

package com.jftse.server.core.shared;

import com.jftse.entities.database.model.Metric;
import com.jftse.entities.database.model.ServerType;
import com.jftse.server.core.util.Time;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MetricsPersistenceService {
    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void flushBatch(List<Map.Entry<MetricsService.MetricKey, MetricsService.AccumulatorSnapshot>> batch) {
        long ts = Time.getNSTime();

        for (Map.Entry<MetricsService.MetricKey, MetricsService.AccumulatorSnapshot> e : batch) {
            MetricsService.MetricKey key = e.getKey();
            MetricsService.AccumulatorSnapshot snap = e.getValue();

            Metric m = getMetric(key.name(), key.serverType());

            long newValue;
            if (snap.hasSet()) {
                newValue = snap.setValue();
            } else {
                long base = (m == null || m.getValue() == null) ? 0L : m.getValue();
                newValue = base;

                if (snap.delta() != 0) {
                    newValue += snap.delta();
                }
                if (snap.sampleCount() > 0) {
                    newValue = snap.sampleSum() / snap.sampleCount();
                }
            }

            if (m == null) {
                m = new Metric();
                m.setName(key.name());
                m.setServerType(key.serverType());
                m.setValue(newValue);
                m.setTimestamp(ts);
                em.persist(m);
            } else {
                m.setValue(newValue);
                m.setTimestamp(ts);
                em.merge(m);
            }
        }

        em.flush();
        em.clear();
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

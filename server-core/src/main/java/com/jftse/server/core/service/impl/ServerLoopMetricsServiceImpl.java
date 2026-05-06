package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.ServerType;
import com.jftse.server.core.BuildInfoProperties;
import com.jftse.server.core.ServerLoopMetrics;
import com.jftse.server.core.rabbit.MetricsPublisher;
import com.jftse.server.core.service.ServerLoopMetricsService;
import com.jftse.server.core.shared.ServerMetricsContext;
import com.jftse.server.core.shared.rabbit.messages.ServerMetricsMessage;
import com.jftse.server.core.util.Time;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ServerLoopMetricsServiceImpl implements ServerLoopMetricsService {
    private long lastMetricsTicks = -1;
    private long lastMetricsTimeMs = -1;

    private final Optional<MetricsPublisher> metricsPublisher;

    @Override
    public void publishMetrics(ServerMetricsContext context) {
        if (metricsPublisher.isEmpty()) {
            return; // metrics disabled
        }

        if (context == null) {
            throw new IllegalArgumentException("ServerMetricsContext cannot be null");
        }

        if (context.serverType() == null || context.buildInfo() == null || context.loopMetrics() == null) {
            throw new IllegalArgumentException("ServerMetricsContext contains null fields");
        }

        final ServerType serverType = context.serverType();
        final BuildInfoProperties buildInfo = context.buildInfo();
        final ServerLoopMetrics metrics = context.loopMetrics();

        final long now = Time.getMSTime();

        final long ticksTotal = metrics.getTicks();
        final double avgTickMsTotal = metrics.getAvgTickMs();
        final long maxTickMsTotal = metrics.getMaxTickMs();
        final double avgUpdateMsTotal = metrics.getAvgUpdateMs();
        final long maxUpdateMsTotal = metrics.getMaxUpdateMs();
        final long maxUpdateMsWindow = metrics.consumeMaxUpdateMsWindow();
        final long maxTickMsWindow = metrics.consumeMaxTickMsWindow();

        long ticksDelta = 0;
        double ticksPerSec = 0.0;
        double avgTickMsWindow = 0.0;

        if (lastMetricsTicks >= 0 && lastMetricsTimeMs >= 0) {
            final long elapsedMs = now - lastMetricsTimeMs;
            final double elapsedSec = elapsedMs / 1000.0;
            ticksDelta = ticksTotal - lastMetricsTicks;

            if (elapsedSec > 0) {
                ticksPerSec = ticksDelta / elapsedSec;
            }

            if (ticksDelta > 0) {
                avgTickMsWindow = (elapsedMs / (double) ticksDelta);
            }
        }

        lastMetricsTicks = ticksTotal;
        lastMetricsTimeMs = now;

        ServerMetricsMessage msg = ServerMetricsMessage.builder()
                .timestampMs(now)
                .server(serverType.getName())
                .revision(buildInfo.getRev())
                .ticksTotal(ticksTotal)
                .ticksDelta(ticksDelta)
                .ticksPerSec(ticksPerSec)
                .avgTickMsTotal(avgTickMsTotal)
                .maxTickMsTotal(maxTickMsTotal)
                .avgUpdateMsTotal(avgUpdateMsTotal)
                .maxUpdateMsTotal(maxUpdateMsTotal)
                .maxUpdateMsWindow(maxUpdateMsWindow)
                .maxTickMsWindow(maxTickMsWindow)
                .avgTickMsWindow(avgTickMsWindow)
                .attributes(context.attributes())
                .build();
        metricsPublisher.get().publish(msg, "system.metrics", "MetricsSystem");
    }
}

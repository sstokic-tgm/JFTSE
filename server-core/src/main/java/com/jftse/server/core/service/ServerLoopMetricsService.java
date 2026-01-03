package com.jftse.server.core.service;

import com.jftse.server.core.shared.ServerMetricsContext;

public interface ServerLoopMetricsService {
    void publishMetrics(ServerMetricsContext context);
}

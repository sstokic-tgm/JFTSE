package com.jftse.server.core.shared;

import com.jftse.entities.database.model.ServerType;
import com.jftse.server.core.BuildInfoProperties;
import com.jftse.server.core.ServerLoopMetrics;

import java.util.HashMap;
import java.util.Map;

public record ServerMetricsContext(ServerType serverType, BuildInfoProperties buildInfo, ServerLoopMetrics loopMetrics,
                                   Map<String, Object> attributes) {

    public static ServerMetricsContext empty() {
        return new ServerMetricsContext(null, null, null, new HashMap<>());
    }

    public static ServerMetricsContext of(ServerType serverType, BuildInfoProperties buildInfo,
                                          ServerLoopMetrics loopMetrics) {
        return new ServerMetricsContext(serverType, buildInfo, loopMetrics, new HashMap<>());
    }

    public ServerMetricsContext attr(String key, Object value) {
        attributes.put(key, value);
        return this;
    }
}

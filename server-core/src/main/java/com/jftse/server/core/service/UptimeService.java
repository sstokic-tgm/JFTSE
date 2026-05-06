package com.jftse.server.core.service;

import com.jftse.entities.database.model.ServerType;
import com.jftse.entities.database.model.Uptime;

public interface UptimeService {
    void save(Uptime uptime);
    void updateUptimeAndMaxPlayers(Long uptime, Integer maxPlayers, ServerType serverType, Long startTime);
}

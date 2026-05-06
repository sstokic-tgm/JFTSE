package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.ServerType;
import com.jftse.entities.database.model.Uptime;
import com.jftse.entities.database.repository.UptimeRepository;
import com.jftse.server.core.service.UptimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UptimeServiceImpl implements UptimeService {
    private final UptimeRepository uptimeRepository;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void save(Uptime uptime) {
        uptimeRepository.save(uptime);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateUptimeAndMaxPlayers(Long uptime, Integer maxPlayers, ServerType serverType, Long startTime) {
        uptimeRepository.updateUptimeAndMaxPlayers(uptime, maxPlayers, serverType, startTime);
    }
}

package com.jftse.server.core.service;

import com.jftse.entities.database.model.player.PlayerStatistic;

import java.util.List;

public interface PlayerStatisticService {
    PlayerStatistic save(PlayerStatistic playerStatistic);

    PlayerStatistic findPlayerStatisticById(Long id);

    List<PlayerStatistic> findAllByIdIn(List<Long> ids);
}

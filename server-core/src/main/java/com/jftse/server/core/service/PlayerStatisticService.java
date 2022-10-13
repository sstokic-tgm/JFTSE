package com.jftse.server.core.service;

import com.jftse.entities.database.model.player.PlayerStatistic;

public interface PlayerStatisticService {
    PlayerStatistic save(PlayerStatistic playerStatistic);

    PlayerStatistic findPlayerStatisticById(Long id);
}

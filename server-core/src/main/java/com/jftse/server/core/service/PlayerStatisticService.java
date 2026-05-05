package com.jftse.server.core.service;

import com.jftse.entities.database.model.player.PlayerStatistic;
import com.jftse.server.core.constants.GameMode;

import java.util.List;

public interface PlayerStatisticService {
    PlayerStatistic save(PlayerStatistic playerStatistic);

    PlayerStatistic findPlayerStatisticById(Long id);

    List<PlayerStatistic> findAllByIdIn(List<Long> ids);

    PlayerStatistic updatePlayerStats(Long playerStatisticId, int gameMode, boolean isWin, int rankingPoints, int serviceAces, int returnAces,
                                      int strokes, int slices, int lobs, int smashes, int volleys, int topSpins, int risings, int serves,
                                      int guardBreakShots, int chargeShots, int skillShots);
}

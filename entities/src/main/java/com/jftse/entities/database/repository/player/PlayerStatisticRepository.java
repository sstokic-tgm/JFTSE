package com.jftse.entities.database.repository.player;

import com.jftse.entities.database.model.player.PlayerStatistic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerStatisticRepository extends JpaRepository<PlayerStatistic, Long> {
    // empty..
}
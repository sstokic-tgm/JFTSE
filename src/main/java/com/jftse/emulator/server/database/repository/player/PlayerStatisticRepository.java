package com.jftse.emulator.server.database.repository.player;

import com.jftse.emulator.server.database.model.player.PlayerStatistic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerStatisticRepository extends JpaRepository<PlayerStatistic, Long> {
    // empty..
}
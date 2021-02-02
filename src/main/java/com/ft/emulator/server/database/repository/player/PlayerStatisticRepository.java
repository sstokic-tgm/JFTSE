package com.ft.emulator.server.database.repository.player;

import com.ft.emulator.server.database.model.player.Player;
import com.ft.emulator.server.database.model.player.PlayerStatistic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerStatisticRepository extends JpaRepository<PlayerStatistic, Long> {
    // empty..
}
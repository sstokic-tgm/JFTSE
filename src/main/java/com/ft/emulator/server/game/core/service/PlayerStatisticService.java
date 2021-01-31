package com.ft.emulator.server.game.core.service;

import com.ft.emulator.server.database.model.player.PlayerStatistic;
import com.ft.emulator.server.database.repository.player.PlayerStatisticRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class PlayerStatisticService {
    private final PlayerStatisticRepository playerStatisticRepository;

    public PlayerStatistic save(PlayerStatistic playerStatistic) {
        return playerStatisticRepository.save(playerStatistic);
    }
}

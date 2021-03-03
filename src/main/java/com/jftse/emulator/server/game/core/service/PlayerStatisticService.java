package com.jftse.emulator.server.game.core.service;

import com.jftse.emulator.server.database.model.player.PlayerStatistic;
import com.jftse.emulator.server.database.repository.player.PlayerStatisticRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class PlayerStatisticService {
    private final PlayerStatisticRepository playerStatisticRepository;

    public PlayerStatistic save(PlayerStatistic playerStatistic) {
        return playerStatisticRepository.save(playerStatistic);
    }

    public PlayerStatistic findPlayerStatisticById(Long id) {
        Optional<PlayerStatistic> playerStatistic = playerStatisticRepository.findById(id);
        return playerStatistic.orElse(null);
    }
}

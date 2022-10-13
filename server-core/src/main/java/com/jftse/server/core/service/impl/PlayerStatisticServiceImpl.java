package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.player.PlayerStatistic;
import com.jftse.entities.database.repository.player.PlayerStatisticRepository;
import com.jftse.server.core.service.PlayerStatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class PlayerStatisticServiceImpl implements PlayerStatisticService {
    private final PlayerStatisticRepository playerStatisticRepository;

    @Override
    public PlayerStatistic save(PlayerStatistic playerStatistic) {
        return playerStatisticRepository.save(playerStatistic);
    }

    @Override
    public PlayerStatistic findPlayerStatisticById(Long id) {
        Optional<PlayerStatistic> playerStatistic = playerStatisticRepository.findById(id);
        return playerStatistic.orElse(null);
    }
}

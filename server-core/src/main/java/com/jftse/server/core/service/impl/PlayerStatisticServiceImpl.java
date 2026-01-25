package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.player.PlayerStatistic;
import com.jftse.entities.database.repository.player.PlayerStatisticRepository;
import com.jftse.server.core.service.PlayerStatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class PlayerStatisticServiceImpl implements PlayerStatisticService {
    private final PlayerStatisticRepository playerStatisticRepository;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PlayerStatistic save(PlayerStatistic playerStatistic) {
        return playerStatisticRepository.save(playerStatistic);
    }

    @Override
    @Transactional(readOnly = true)
    public PlayerStatistic findPlayerStatisticById(Long id) {
        Optional<PlayerStatistic> playerStatistic = playerStatisticRepository.findById(id);
        return playerStatistic.orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlayerStatistic> findAllByIdIn(List<Long> ids) {
        return playerStatisticRepository.findAllByIdIn(ids);
    }
}

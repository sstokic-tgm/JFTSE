package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.log.GameLog;
import com.jftse.entities.database.repository.log.GameLogRepository;
import com.jftse.server.core.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameLogServiceImpl implements GameLogService {
    private final GameLogRepository gameLogRepository;

    @Override
    public GameLog save(GameLog gameLog) {
        return gameLogRepository.save(gameLog);
    }
}

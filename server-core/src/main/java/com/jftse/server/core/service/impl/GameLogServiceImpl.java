package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.log.GameLog;
import com.jftse.entities.database.repository.log.GameLogRepository;
import com.jftse.server.core.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class GameLogServiceImpl implements GameLogService {
    private final GameLogRepository gameLogRepository;

    @Override
    public GameLog save(GameLog gameLog) {
        return gameLogRepository.save(gameLog);
    }
}

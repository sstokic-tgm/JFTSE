package com.jftse.emulator.server.core.service;

import com.jftse.entities.database.model.log.GameLog;
import com.jftse.entities.database.repository.log.GameLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class GameLogService {
    private final GameLogRepository gameLogRepository;

    public GameLog save(GameLog gameLog) {
        return gameLogRepository.save(gameLog);
    }
}

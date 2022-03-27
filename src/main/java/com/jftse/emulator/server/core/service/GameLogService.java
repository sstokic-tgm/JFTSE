package com.jftse.emulator.server.core.service;

import com.jftse.emulator.server.database.model.GameLog;
import com.jftse.emulator.server.database.repository.GameLogRepository;
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

package com.jftse.server.core.service;

import com.jftse.entities.database.model.log.GameLog;

public interface GameLogService {
    GameLog save(GameLog gameLog);
}

package com.jftse.server.core.service;

import com.jftse.entities.database.model.log.CommandLog;

import java.util.List;

public interface CommandLogService {
    CommandLog save(CommandLog commandLog);
    List<CommandLog> findAllByPlayerId(Long playerId);
}

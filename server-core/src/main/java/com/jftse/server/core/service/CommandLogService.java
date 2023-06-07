package com.jftse.server.core.service;

import com.jftse.entities.database.model.log.CommandLog;

public interface CommandLogService {
    CommandLog save(CommandLog commandLog);
}

package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.log.CommandLog;
import com.jftse.entities.database.repository.log.CommandLogRepository;
import com.jftse.server.core.service.CommandLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class CommandLogServiceImpl implements CommandLogService {
    private final CommandLogRepository commandLogRepository;

    @Override
    public CommandLog save(CommandLog commandLog) {
        return commandLogRepository.save(commandLog);
    }
}

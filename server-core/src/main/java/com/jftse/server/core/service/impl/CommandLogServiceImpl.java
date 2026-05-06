package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.log.CommandLog;
import com.jftse.entities.database.repository.log.CommandLogRepository;
import com.jftse.server.core.service.CommandLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommandLogServiceImpl implements CommandLogService {
    private final CommandLogRepository commandLogRepository;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CommandLog save(CommandLog commandLog) {
        return commandLogRepository.save(commandLog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommandLog> findAllByPlayerId(Long playerId) {
        return commandLogRepository.findAllByPlayerId(playerId);
    }
}

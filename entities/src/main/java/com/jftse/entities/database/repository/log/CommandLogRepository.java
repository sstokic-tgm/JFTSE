package com.jftse.entities.database.repository.log;

import com.jftse.entities.database.model.log.CommandLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommandLogRepository extends JpaRepository<CommandLog, Long> {
    List<CommandLog> findAllByPlayerId(Long playerId);
}

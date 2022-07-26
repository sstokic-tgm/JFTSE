package com.jftse.entities.database.repository.log;

import com.jftse.entities.database.model.log.GameLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameLogRepository extends JpaRepository<GameLog, Long> {
}

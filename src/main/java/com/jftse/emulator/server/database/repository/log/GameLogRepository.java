package com.jftse.emulator.server.database.repository.log;

import com.jftse.emulator.server.database.model.log.GameLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameLogRepository extends JpaRepository<GameLog, Long> {
}

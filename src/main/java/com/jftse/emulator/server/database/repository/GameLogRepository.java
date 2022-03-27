package com.jftse.emulator.server.database.repository;

import com.jftse.emulator.server.database.model.GameLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameLogRepository extends JpaRepository<GameLog, Long> {
}

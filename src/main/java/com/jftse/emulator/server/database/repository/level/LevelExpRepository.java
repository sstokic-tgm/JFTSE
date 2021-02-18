package com.jftse.emulator.server.database.repository.level;

import com.jftse.emulator.server.database.model.level.LevelExp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LevelExpRepository extends JpaRepository<LevelExp, Long> {
    List<LevelExp> findAllByLevel(Byte level);
}

package com.jftse.entities.database.repository.level;

import com.jftse.entities.database.model.level.LevelExp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LevelExpRepository extends JpaRepository<LevelExp, Long> {
    List<LevelExp> findAllByLevel(Byte level);
}

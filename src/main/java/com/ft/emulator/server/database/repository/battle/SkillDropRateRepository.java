package com.ft.emulator.server.database.repository.battle;

import com.ft.emulator.server.database.model.battle.SkillDropRate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillDropRateRepository extends JpaRepository<SkillDropRate, Long> {
}

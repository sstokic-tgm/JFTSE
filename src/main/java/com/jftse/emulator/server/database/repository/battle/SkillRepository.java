package com.jftse.emulator.server.database.repository.battle;

import com.jftse.emulator.server.database.model.battle.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillRepository extends JpaRepository<Skill, Long> {
}

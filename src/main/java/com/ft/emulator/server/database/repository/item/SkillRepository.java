package com.ft.emulator.server.database.repository.item;

import com.ft.emulator.server.database.model.battle.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillRepository extends JpaRepository<Skill, Long> {
}

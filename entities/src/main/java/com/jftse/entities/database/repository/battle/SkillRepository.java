package com.jftse.entities.database.repository.battle;

import com.jftse.entities.database.model.battle.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillRepository extends JpaRepository<Skill, Long> {
}

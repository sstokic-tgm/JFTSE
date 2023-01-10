package com.jftse.server.core.service;

import com.jftse.entities.database.model.battle.Skill;

public interface SkillService {
    Skill findSkillById(Long id);
}

package com.jftse.server.core.service;

import com.jftse.entities.database.model.battle.Skill;
import com.jftse.entities.database.model.map.SMaps;
import com.jftse.entities.database.model.scenario.MScenarios;

public interface GuardianSkillsService {
    Skill getRandomGuardianSkillBasedOnProbability(int btItemId, int guardianId, boolean isBoss, MScenarios scenario, SMaps map);
}

package com.jftse.server.core.service;

import com.jftse.entities.database.model.battle.Skill;
import com.jftse.entities.database.model.battle.Skill2Guardians;
import com.jftse.entities.database.model.map.SMaps;
import com.jftse.entities.database.model.scenario.MScenarios;

import java.util.List;

public interface GuardianSkillsService {
    Skill getRandomGuardianSkillBasedOnProbability(int btItemId, int guardianId, boolean isBoss, MScenarios scenario, SMaps map);
    List<Skill2Guardians> findAllByBtItemId(Integer btItemId);
    List<Skill2Guardians> getSkillsByMapAndGuardianAndScenario(Long mapId, Long guardianId, Long scenarioId);
    List<Skill2Guardians> getSkillsByMapAndBossGuardianAndScenario(Long mapId, Long guardianId, Long scenarioId);
}

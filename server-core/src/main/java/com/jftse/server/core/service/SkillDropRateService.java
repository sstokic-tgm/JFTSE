package com.jftse.server.core.service;

import com.jftse.entities.database.model.battle.SkillDropRate;

import java.util.List;

public interface SkillDropRateService {
    SkillDropRate findSkillDropRateByPlayerLevel(int playerLevel);
    List<Integer> getDropRatesForSkillDropRate(SkillDropRate skillDropRate);
}

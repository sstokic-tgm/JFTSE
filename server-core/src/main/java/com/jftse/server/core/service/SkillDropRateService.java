package com.jftse.server.core.service;

import com.jftse.entities.database.model.battle.SkillDropRate;
import com.jftse.entities.database.model.player.Player;

public interface SkillDropRateService {
    SkillDropRate findSkillDropRateByPlayerLevel(int playerLevel);
}

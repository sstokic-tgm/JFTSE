package com.jftse.server.core.service;

import com.jftse.entities.database.model.level.LevelExp;
import com.jftse.entities.database.model.player.Player;

import java.util.List;

public interface LevelService {
    byte getLevel(int expValue, int currentExp, byte currentLevel);

    List<LevelExp> findAllByExpValue(Integer expValue);

    Player setNewLevelStatusPoints(byte level, Player player);
}

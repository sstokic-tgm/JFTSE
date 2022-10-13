package com.jftse.server.core.service;

import com.jftse.entities.database.model.player.Player;

public interface LevelService {
    byte getLevel(int expValue, int currentExp, byte currentLevel);

    Player setNewLevelStatusPoints(byte level, Player player);
}

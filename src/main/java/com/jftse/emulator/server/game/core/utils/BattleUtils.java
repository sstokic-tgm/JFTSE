package com.jftse.emulator.server.game.core.utils;

import com.jftse.emulator.server.database.model.player.Player;

public class BattleUtils {
    /**
    * hp = base hp + 3hp for each level; level - 1 because at level 1 we have the base hp
    */
    public static int calculatePlayerHp(Player player) {
        return 200 + (3 * (player.getLevel() - 1));
    }
}

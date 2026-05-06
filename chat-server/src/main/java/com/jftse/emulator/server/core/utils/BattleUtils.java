package com.jftse.emulator.server.core.utils;

public class BattleUtils {
    /**
     * hp = base hp + 3hp for each level; level - 1 because at level 1 we have the base hp
     */
    public static int calculatePlayerHp(int level) {
        return 200 + (5 * (level - 1));
    }
}

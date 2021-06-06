package com.jftse.emulator.server.game.core.utils;

import com.jftse.emulator.server.database.model.battle.WillDamage;
import com.jftse.emulator.server.database.model.player.Player;

import java.util.Arrays;
import java.util.List;

public class BattleUtils {
    private static List<Integer> StrTable = Arrays.asList(
            5, 7, 9, 12, 15, 18, 20, 23, 26, 29, 32, 35, 38, 41, 44, 46, 49, 52, 55, 58, 61, 63, 66, 69,
            72, 75, 78, 81, 83, 86, 89, 92, 95, 98, 101, 102, 106, 109, 112, 115, 118, 121, 124
    );

    private static List<Integer> StaTable = Arrays.asList(
            7, 10, 14, 17, 20, 24, 27, 30, 34, 37, 40, 44, 47, 50, 54, 57, 60, 64, 67, 70, 74, 77, 80, 84,
            87, 90, 94, 97, 100, 104, 107, 110, 114, 117, 120, 124, 127
    );

    /**
     * hp = base hp + 3hp for each level; level - 1 because at level 1 we have the base hp
     */
    public static int calculatePlayerHp(Player player) {
        return 200 + (3 * (player.getLevel() - 1));
    }

    public static int calculateDmg(int str, int baseDmg, boolean hasStrBuff) {
        int additionalDmg = (int) StrTable.stream().filter(x -> x <= str).count();
        int totalDmg = baseDmg - additionalDmg;
        if (hasStrBuff) {
            totalDmg -= Math.abs(totalDmg) * 0.2;
        }

        return totalDmg;
    }

    public static int calculateDef(int sta, int dmg, boolean hasDefBuff) {
        int dmgToDeny = (int) StaTable.stream().filter(x -> x <= sta).count();
        if (hasDefBuff) {
            dmgToDeny += dmg * 0.2;
        }

        return dmgToDeny;
    }

    public static int calculateBallDamageByWill(WillDamage willDamage, boolean hasWillBuff) {
        int ballDamage = willDamage.getDamage();
        if (hasWillBuff) {
            ballDamage += ballDamage * 0.2;
        }

        return ballDamage;
    }
}

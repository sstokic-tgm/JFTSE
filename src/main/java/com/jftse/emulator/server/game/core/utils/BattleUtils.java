package com.jftse.emulator.server.game.core.utils;

import com.jftse.emulator.server.database.model.battle.WillDamage;
import com.jftse.emulator.server.database.model.player.Player;

public class BattleUtils {
    /**
     * hp = base hp + 3hp for each level; level - 1 because at level 1 we have the base hp
     */
    public static int calculatePlayerHp(Player player) {
        return 200 + (3 * (player.getLevel() - 1));
    }

    public static int calculateDmg(int str, int baseDmg, boolean hasStrBuff) {
        int distanceSection = 0;
        int i = 0;
        int step = 0;
        do {
            if (i > 0 && (i % 20) == 0) {
                distanceSection++;
                step = 0;
            } else if (step != 0 && (step % 3) == 0) {
                distanceSection++;
            }

            step++;
            i++;
        } while (i <= str);

        int totalDmg = baseDmg - distanceSection;
        if (hasStrBuff) {
            totalDmg -= Math.abs(totalDmg) * 0.2;
        }

        return totalDmg;
    }

    public static int calculateDef(int sta, int dmg, boolean hasDefBuff) {
        int distanceSection = 0;
        int i = 0;
        int step = -1;
        do {
            if (i > 0 && (i % 10) == 0) {
                distanceSection++;
                step = -1;
            } else if (step != 0 && (step % 3) == 0) {
                distanceSection++;
                step = 0;
            }

            step++;
            i++;
        } while (i <= sta);

        int dmgToDeny = distanceSection;
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

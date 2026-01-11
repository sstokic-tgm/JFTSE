package com.jftse.emulator.server.core.utils;

import com.jftse.emulator.server.core.manager.GameManager;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.atomic.AtomicReference;

@Log4j2
public class BattleUtils {
    private static final String K_STR_SCALE = "StrengthDamageScale";
    private static final String K_STA_SCALE = "StaminaDamageReductionScale";
    private static final String K_WIL_SCALE = "WillpowerBallDamageScale";
    private static final String K_BALL_BASE = "BallBaseDamage";
    private static final String K_BALL_MIN = "BallMinDamage";

    private static final AtomicReference<StatConfig> statConfig = new AtomicReference<>();

    private static StatConfig config() {
        return statConfig.get();
    }

    /**
     * hp = base hp + 3hp for each level; level - 1 because at level 1 we have the base hp
     */
    public static int calculatePlayerHp(int level) {
        return 200 + (5 * (level - 1));
    }

    public static int calculateDmg(int str, int baseDmg, boolean hasStrBuff) {
        int statBonus = (int) (str * config().strengthScale());
        int totalDmg = baseDmg - statBonus;
        if (hasStrBuff) {
            totalDmg -= (int) (Math.abs(totalDmg) * 0.2);
        }
        return totalDmg;
    }

    public static int calculateDef(int sta, int dmg, boolean hasDefBuff) {
        int dmgToDeny = (int) (sta * config().staminaScale());
        if (hasDefBuff) {
            dmgToDeny += (int) (dmg * 0.2);
        }
        return dmgToDeny;
    }

    public static int calculateBallDmg(int wil, boolean hasWillBuff) {
        int ballDmg = config().ballBaseDamage() + (int) (wil * config().willpowerScale());
        if (hasWillBuff) {
            ballDmg += (int) (ballDmg * 0.2);
        }
        return Math.max(config().ballMinDamage(), ballDmg);
    }

    private static int calculateStatBonus(int stat, int bonusPerSection, int sectionSize) {
        return (stat * bonusPerSection) / sectionSize;
    }

    public static void reloadStatConfig() {
        if (!statConfig.compareAndSet(statConfig.get(), getStatConfig())) {
            log.error("Failed to reload battle stat configuration");
        }
    }

    public static void loadStatConfig() {
        if (!statConfig.compareAndSet(null, getStatConfig())) {
            log.error("Failed to load battle stat configuration");
        } else {
            log.info("Battle stat configuration loaded...\n{}", statConfig.get().toString());
        }
    }

    private static StatConfig getStatConfig() {
        GameManager gameManager = GameManager.getInstance();
        double strScale = gameManager.getServerConfService().get(K_STR_SCALE, Double.class);
        double staScale = gameManager.getServerConfService().get(K_STA_SCALE, Double.class);
        double wilScale = gameManager.getServerConfService().get(K_WIL_SCALE, Double.class);
        int ballBaseDmg = gameManager.getServerConfService().get(K_BALL_BASE, Integer.class);
        int ballMinDmg = gameManager.getServerConfService().get(K_BALL_MIN, Integer.class);

        // we dont want negative or zero values so we default it to client values
        if (strScale < 0) strScale = 0.35;
        if (staScale < 0) staScale = 0.30;
        if (wilScale < 0) wilScale = 0.52;
        if (ballBaseDmg < 0) ballBaseDmg = 10;
        if (ballMinDmg < 0) ballMinDmg = 20;

        return new StatConfig(strScale, staScale, wilScale, ballBaseDmg, ballMinDmg);
    }

    private record StatConfig(double strengthScale, double staminaScale, double willpowerScale, int ballBaseDamage,
                              int ballMinDamage) {

        @Override
        public String toString() {
            return "[strengthScale=" + strengthScale +
                    ", staminaScale=" + staminaScale +
                    ", willpowerScale=" + willpowerScale +
                    ", ballBaseDamage=" + ballBaseDamage +
                    ", ballMinDamage=" + ballMinDamage + "]";
        }
    }
}

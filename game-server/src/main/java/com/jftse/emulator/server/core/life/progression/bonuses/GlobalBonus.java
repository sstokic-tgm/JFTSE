package com.jftse.emulator.server.core.life.progression.bonuses;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.server.core.life.progression.ExpGoldBonus;
import com.jftse.emulator.server.core.life.progression.ExpGoldBonusDecorator;

public class GlobalBonus extends ExpGoldBonusDecorator {
    public GlobalBonus(ExpGoldBonus expGoldBonus) {
        super(expGoldBonus);
    }

    @Override
    public int calculateExp() {
        return super.calculateExp() * globalExp();
    }

    @Override
    public int calculateGold() {
        return super.calculateGold() * globalGold();
    }

    private int globalExp() {
        return ConfigService.getInstance().getValue("game.bonus.global.exp", 5);
    }

    private int globalGold() {
        return ConfigService.getInstance().getValue("game.bonus.global.gold", 5);
    }
}

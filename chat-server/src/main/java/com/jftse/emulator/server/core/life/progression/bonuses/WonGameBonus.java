package com.jftse.emulator.server.core.life.progression.bonuses;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.server.core.life.progression.ExpGoldBonus;
import com.jftse.emulator.server.core.life.progression.ExpGoldBonusDecorator;

public class WonGameBonus extends ExpGoldBonusDecorator {
    private final double expModifier;
    private final double goldModifier;

    public WonGameBonus(ExpGoldBonus expGoldBonus) {
        super(expGoldBonus);

        this.expModifier = ConfigService.getInstance().getValue("game.bonus.wongame.exp", 0.2);
        this.goldModifier = ConfigService.getInstance().getValue("game.bonus.wongame.gold", 0.2);
    }

    @Override
    public int calculateExp() {
        int calculatedExp = super.calculateExp();
        return (int) (calculatedExp + (calculatedExp * expModifier));
    }

    @Override
    public int calculateGold() {
        int calculatedGold = super.calculateGold();
        return (int) (calculatedGold + (calculatedGold * goldModifier));
    }
}

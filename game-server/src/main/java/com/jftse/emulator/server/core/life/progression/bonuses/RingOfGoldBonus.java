package com.jftse.emulator.server.core.life.progression.bonuses;

import com.jftse.emulator.server.core.life.progression.ExpGoldBonus;
import com.jftse.emulator.server.core.life.progression.ExpGoldBonusDecorator;

public class RingOfGoldBonus extends ExpGoldBonusDecorator {
    public RingOfGoldBonus(ExpGoldBonus expGoldBonus) {
        super(expGoldBonus);
    }

    @Override
    public int calculateExp() {
        int calculatedExp = super.calculateExp();
        return calculatedExp + (calculatedExp * 2);
    }

    @Override
    public int calculateGold() {
        int calculatedGold = super.calculateGold();
        return calculatedGold + (calculatedGold * 2);
    }
}

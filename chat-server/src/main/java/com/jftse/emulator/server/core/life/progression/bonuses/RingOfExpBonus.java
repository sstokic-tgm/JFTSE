package com.jftse.emulator.server.core.life.progression.bonuses;

import com.jftse.emulator.server.core.life.progression.ExpGoldBonus;
import com.jftse.emulator.server.core.life.progression.ExpGoldBonusDecorator;

public class RingOfExpBonus extends ExpGoldBonusDecorator {
    public RingOfExpBonus(ExpGoldBonus expGoldBonus) {
        super(expGoldBonus);
    }

    @Override
    public int calculateExp() {
        int calculatedExp = super.calculateExp();
        return calculatedExp * 2;
    }

    @Override
    public int calculateGold() {
        return super.calculateGold();
    }
}

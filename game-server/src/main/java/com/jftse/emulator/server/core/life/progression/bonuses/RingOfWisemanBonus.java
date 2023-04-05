package com.jftse.emulator.server.core.life.progression.bonuses;

import com.jftse.emulator.server.core.life.progression.ExpGoldBonus;
import com.jftse.emulator.server.core.life.progression.ExpGoldBonusDecorator;

public class RingOfWisemanBonus extends ExpGoldBonusDecorator {
    public RingOfWisemanBonus(ExpGoldBonus expGoldBonus) {
        super(expGoldBonus);
    }

    @Override
    public int calculateExp() {
        int calculatedExp = super.calculateExp();
        return (int) (calculatedExp + (calculatedExp * 1.5));
    }

    @Override
    public int calculateGold() {
        int calculatedGold = super.calculateGold();
        return (int) (calculatedGold + (calculatedGold * 1.5));
    }
}

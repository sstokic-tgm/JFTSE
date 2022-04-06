package com.jftse.emulator.server.core.life.progression;

public abstract class ExpGoldBonusDecorator implements ExpGoldBonus {
    private final ExpGoldBonus wrapper;

    protected ExpGoldBonusDecorator(ExpGoldBonus expGoldBonus) {
        this.wrapper = expGoldBonus;
    }

    @Override
    public int calculateExp() {
        return wrapper.calculateExp();
    }

    @Override
    public int calculateGold() {
        return wrapper.calculateGold();
    }
}

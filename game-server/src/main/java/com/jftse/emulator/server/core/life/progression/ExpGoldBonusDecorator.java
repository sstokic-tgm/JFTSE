package com.jftse.emulator.server.core.life.progression;

public abstract class ExpGoldBonusDecorator implements ExpGoldBonus {
    protected final ExpGoldBonus wrapper;

    protected ExpGoldBonusDecorator(ExpGoldBonus wrapper) {
        this.wrapper = wrapper;
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

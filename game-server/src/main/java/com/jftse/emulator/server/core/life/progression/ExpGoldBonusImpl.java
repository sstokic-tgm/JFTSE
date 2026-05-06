package com.jftse.emulator.server.core.life.progression;

public class ExpGoldBonusImpl implements ExpGoldBonus {
    private final int exp;
    private final int gold;

    public ExpGoldBonusImpl(int exp, int gold) {
        this.exp = exp;
        this.gold = gold;
    }

    @Override
    public int calculateExp() {
        return exp;
    }

    @Override
    public int calculateGold() {
        return gold;
    }
}

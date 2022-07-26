package com.jftse.emulator.server.core.life.progression;

public class SimpleExpGoldBonus extends ExpGoldBonusDecorator {
    private Double bonus = null;

    public SimpleExpGoldBonus(ExpGoldBonus expGoldBonus) {
        super(expGoldBonus);
    }

    public SimpleExpGoldBonus(ExpGoldBonus expGoldBonus, double bonus) {
        super(expGoldBonus);

        this.bonus = bonus;
    }

    @Override
    public int calculateExp() {
        if (bonus == null) {
            return super.calculateExp();
        } else {
            int calculatedExp = super.calculateExp();
            return (int) (calculatedExp + (calculatedExp * bonus));
        }
    }

    @Override
    public int calculateGold() {
        if (bonus == null) {
            return super.calculateGold();
        } else {
            int calculatedGold = super.calculateGold();
            return (int) (calculatedGold + (calculatedGold * bonus));
        }
    }
}

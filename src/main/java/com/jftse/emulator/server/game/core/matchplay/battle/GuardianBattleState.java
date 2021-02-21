package com.jftse.emulator.server.game.core.matchplay.battle;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GuardianBattleState {
    private short position;
    private int maxHealth;
    private int currentHealth;
    private int str;
    private int sta;
    private int dex;
    private int will;
    private int exp;
    private int gold;

    public GuardianBattleState(short position, int hp, int str, int sta, int dex, int will, int exp, int gold) {
        this.position = position;
        this.maxHealth = hp;
        this.currentHealth = hp;
        this.str = str;
        this.sta = sta;
        this.dex = dex;
        this.exp = exp;
        this.gold = gold;
    }
}

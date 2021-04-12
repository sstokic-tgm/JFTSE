package com.jftse.emulator.server.game.core.matchplay.battle;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class PlayerBattleState {
    private short position;
    private short maxHealth;
    private short currentHealth;
    private int str;
    private int sta;
    private int dex;
    private int will;
    private boolean dead;

    public PlayerBattleState(short position, short hp, int str, int sta, int dex, int will) {
        this.position = position;
        this.maxHealth = hp;
        this.currentHealth = hp;
        this.str = str;
        this.sta = sta;
        this.dex = dex;
        this.will = will;
    }
}

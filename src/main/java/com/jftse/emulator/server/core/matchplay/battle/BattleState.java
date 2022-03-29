package com.jftse.emulator.server.core.matchplay.battle;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class BattleState {
    protected int position;

    protected int maxHealth;
    protected AtomicInteger currentHealth;

    protected int str;
    protected int sta;
    protected int dex;
    protected int will;

    protected boolean dead;
    protected boolean shieldActive;
    protected boolean miniamActive;
    protected boolean apollonFlashActive;
    protected LinkedBlockingDeque<SkillUse> skillUseDeque;

    protected BattleState(short position, int hp, int str, int sta, int dex, int will) {
        this();

        this.position = position;
        this.maxHealth = hp;
        this.currentHealth = new AtomicInteger(hp);

        this.str = str;
        this.sta = sta;
        this.dex = dex;
        this.will = will;
    }

    protected BattleState() {
        shieldActive = false;
        miniamActive = false;
        apollonFlashActive = false;

        skillUseDeque = new LinkedBlockingDeque<>();
    }
}

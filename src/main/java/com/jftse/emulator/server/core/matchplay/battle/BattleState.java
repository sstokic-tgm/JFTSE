package com.jftse.emulator.server.core.matchplay.battle;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class BattleState {
    protected AtomicInteger position;

    protected AtomicInteger maxHealth;
    protected AtomicInteger currentHealth;

    protected AtomicInteger str;
    protected AtomicInteger sta;
    protected AtomicInteger dex;
    protected AtomicInteger will;

    protected AtomicBoolean dead;
    protected AtomicBoolean shieldActive;
    protected AtomicBoolean miniamActive;
    protected AtomicBoolean apollonFlashActive;
    protected LinkedBlockingDeque<SkillUse> skillUseDeque;

    protected BattleState(short position, int hp, int str, int sta, int dex, int will) {
        this();

        this.position = new AtomicInteger(position);
        this.maxHealth = new AtomicInteger(hp);
        this.currentHealth = new AtomicInteger(hp);

        this.str = new AtomicInteger(str);
        this.sta = new AtomicInteger(sta);
        this.dex = new AtomicInteger(dex);
        this.will = new AtomicInteger(will);
    }

    protected BattleState() {

        shieldActive = new AtomicBoolean(false);
        miniamActive = new AtomicBoolean(false);
        apollonFlashActive = new AtomicBoolean(false);

        skillUseDeque = new LinkedBlockingDeque<>();
    }
}

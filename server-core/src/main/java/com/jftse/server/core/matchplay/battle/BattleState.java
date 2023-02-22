package com.jftse.server.core.matchplay.battle;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ConcurrentHashMap;
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
    protected ConcurrentHashMap<Integer, SkillUse> quickSlotSkillUseMap;
    protected AtomicInteger quickSlotSkillUseNoCDDetects;

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

        quickSlotSkillUseMap = new ConcurrentHashMap<>();
        quickSlotSkillUseNoCDDetects = new AtomicInteger(0);
    }
}

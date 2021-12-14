package com.jftse.emulator.server.core.matchplay.battle;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class GuardianBattleState {
    private final int id;
    private AtomicInteger btItemId;
    private AtomicInteger position;
    private AtomicInteger maxHealth;
    private AtomicInteger currentHealth;
    private AtomicInteger str;
    private AtomicInteger sta;
    private AtomicInteger dex;
    private AtomicInteger will;
    private final int exp;
    private final int gold;
    private AtomicBoolean looted;
    private ConcurrentHashMap<Long, Long> lastSkillHitsTarget;

    private AtomicBoolean shieldActive;
    private AtomicBoolean miniamActive;
    private AtomicBoolean apollonFlashActive;
    private LinkedBlockingDeque<SkillUse> skillUseDeque;

    public GuardianBattleState(int id, int btItemId, short position, int hp, int str, int sta, int dex, int will, int exp, int gold) {
        this.id = id;
        this.btItemId = new AtomicInteger(btItemId);
        this.position = new AtomicInteger(position);
        this.maxHealth = new AtomicInteger(hp);
        this.currentHealth = new AtomicInteger(hp);
        this.str = new AtomicInteger(str);
        this.sta = new AtomicInteger(sta);
        this.dex = new AtomicInteger(dex);
        this.will = new AtomicInteger(will);
        this.exp = exp;
        this.gold = gold;
        this.looted = new AtomicBoolean(false);
        lastSkillHitsTarget = new ConcurrentHashMap<>(5);

        shieldActive = new AtomicBoolean(false);
        miniamActive = new AtomicBoolean(false);
        apollonFlashActive = new AtomicBoolean(false);
        skillUseDeque = new LinkedBlockingDeque<>();
    }
}

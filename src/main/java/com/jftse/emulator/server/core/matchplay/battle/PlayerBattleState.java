package com.jftse.emulator.server.core.matchplay.battle;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class PlayerBattleState {
    private AtomicInteger position;
    private AtomicInteger maxHealth;
    private AtomicInteger currentHealth;
    private AtomicInteger str;
    private AtomicInteger sta;
    private AtomicInteger dex;
    private AtomicInteger will;
    private AtomicBoolean dead;
    private ConcurrentHashMap<Long, Long> lastQS;
    private ConcurrentHashMap<Long, Integer> lastQSCounter;

    private AtomicBoolean shieldActive;
    private AtomicBoolean miniamActive;
    private AtomicBoolean apollonFlashActive;
    private LinkedBlockingDeque<SkillUse> skillUseDeque;

    public PlayerBattleState(short position, short hp, int str, int sta, int dex, int will) {
        this.position = new AtomicInteger(position);
        this.maxHealth = new AtomicInteger(hp);
        this.currentHealth = new AtomicInteger(hp);
        this.str = new AtomicInteger(str);
        this.sta = new AtomicInteger(sta);
        this.dex = new AtomicInteger(dex);
        this.will = new AtomicInteger(will);
        this.dead = new AtomicBoolean(false);
        lastQS = new ConcurrentHashMap<>();
        lastQSCounter = new ConcurrentHashMap<>();

        shieldActive = new AtomicBoolean(false);
        miniamActive = new AtomicBoolean(false);
        apollonFlashActive = new AtomicBoolean(false);
        skillUseDeque = new LinkedBlockingDeque<>();
    }
}

package com.jftse.emulator.server.core.matchplay.battle;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;

@Getter
@Setter
public class PlayerBattleState {
    private int position;
    private int maxHealth;
    private int currentHealth;
    private int str;
    private int sta;
    private int dex;
    private int will;
    private boolean dead;
    private HashMap<Long, Long> lastQS;
    private HashMap<Long, Integer> lastQSCounter;

    private boolean shieldActive;
    private boolean miniamActive;
    private boolean apollonFlashActive;
    private ArrayList<SkillUse> skillUseList;

    public PlayerBattleState(short position, short hp, int str, int sta, int dex, int will) {
        this.position = position;
        this.maxHealth = hp;
        this.currentHealth = hp;
        this.str = str;
        this.sta = sta;
        this.dex = dex;
        this.will = will;
        this.dead = false;
        lastQS = new HashMap<>();
        lastQSCounter = new HashMap<>();

        shieldActive = false;
        miniamActive = false;
        apollonFlashActive = false;
        skillUseList = new ArrayList<>();
    }
}

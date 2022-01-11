package com.jftse.emulator.server.core.matchplay.battle;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;

@Getter
@Setter
public class GuardianBattleState {
    private final int id;
    private int btItemId;
    private int position;
    private int maxHealth;
    private int currentHealth;
    private int str;
    private int sta;
    private int dex;
    private int will;
    private final int exp;
    private final int gold;
    private boolean looted;
    private HashMap<Long, Long> lastSkillHitsTarget;

    private boolean shieldActive;
    private boolean miniamActive;
    private boolean apollonFlashActive;
    private ArrayList<SkillUse> skillUseDeque;

    public GuardianBattleState(int id, int btItemId, short position, int hp, int str, int sta, int dex, int will, int exp, int gold) {
        this.id = id;
        this.btItemId = btItemId;
        this.position = position;
        this.maxHealth = hp;
        this.currentHealth = hp;
        this.str = str;
        this.sta = sta;
        this.dex = dex;
        this.will = will;
        this.exp = exp;
        this.gold = gold;
        this.looted = false;
        lastSkillHitsTarget = new HashMap<>(5);

        shieldActive = false;
        miniamActive = false;
        apollonFlashActive = false;
        skillUseDeque = new ArrayList<>();
    }
}

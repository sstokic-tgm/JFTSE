package com.ft.emulator.server.game.core.matchplay.battle;

import com.ft.emulator.server.database.model.battle.Guardian;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class GuardianBattleState {
    private short position;
    private short maxHealth;
    private short currentHealth;
    private byte str;
    private byte sta;
    private byte dex;
    private byte will;

    public GuardianBattleState(short position, short hp, byte str, byte sta, byte dex, byte will) {
        this.position = position;
        this.maxHealth = hp;
        this.currentHealth = hp;
        this.str = str;
        this.sta = sta;
        this.dex = dex;
        this.will = will;
    }
}

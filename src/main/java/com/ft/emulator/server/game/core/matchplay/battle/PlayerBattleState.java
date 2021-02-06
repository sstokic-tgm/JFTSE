package com.ft.emulator.server.game.core.matchplay.battle;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class PlayerBattleState {
    private short maxHealth;
    private short currentHealth;
    private List<Short> skillsStack;

    public PlayerBattleState() {
        this.skillsStack = Arrays.asList((short) -1, (short) -1);
    }
}

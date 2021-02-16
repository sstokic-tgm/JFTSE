package com.ft.emulator.server.game.core.matchplay.battle;

import com.ft.emulator.server.database.model.battle.Guardian;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class GuardianBattleState {
    private Guardian guardian;
    private short position;
    private short maxHealth;
    private short currentHealth;
}

package com.ft.emulator.server.game.core.matchplay.battle;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class PlayerBattleState {
    private short maxPlayerHealth;
    private short currentPlayerHealth;
    private List<Short> playerSkills;

    public PlayerBattleState() {
        this.playerSkills = Arrays.asList((short) -1, (short) -1);
    }
}

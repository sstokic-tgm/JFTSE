package com.jftse.emulator.server.core.matchplay.battle;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlayerBattleState extends BattleState {

    public PlayerBattleState(short position, int hp, int str, int sta, int dex, int will) {
        super(position, hp, str, sta, dex, will);

        this.dead = false;
    }
}

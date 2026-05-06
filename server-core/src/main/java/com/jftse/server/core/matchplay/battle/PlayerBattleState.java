package com.jftse.server.core.matchplay.battle;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlayerBattleState extends BattleState {
    private final long id;

    public PlayerBattleState(short position, long playerId, int hp, int str, int sta, int dex, int will) {
        super(position, hp, str, sta, dex, will);

        this.dead = false;
        this.id = playerId;
    }
}

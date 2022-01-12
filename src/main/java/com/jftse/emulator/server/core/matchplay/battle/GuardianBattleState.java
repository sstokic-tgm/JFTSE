package com.jftse.emulator.server.core.matchplay.battle;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicBoolean;

@Getter
@Setter
public class GuardianBattleState extends BattleState {
    private final int id;
    private final int btItemId;

    private final int exp;
    private final int gold;
    private AtomicBoolean looted;

    public GuardianBattleState(int id, int btItemId, short position, int hp, int str, int sta, int dex, int will, int exp, int gold) {
        super(position, hp, str, sta, dex, will);

        this.id = id;
        this.btItemId = btItemId;

        this.exp = exp;
        this.gold = gold;
        this.looted = new AtomicBoolean(false);
    }
}

package com.jftse.server.core.matchplay.battle;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GuardianBattleState extends BattleState {
    private final int id;
    private final int btItemId;

    private final int exp;
    private final int gold;
    private final int rewardRankingPoint;
    private boolean looted;

    public GuardianBattleState(int id, int btItemId, short position, int hp, int str, int sta, int dex, int will, int exp, int gold, int rewardRankingPoint) {
        super(position, hp, str, sta, dex, will);

        this.id = id;
        this.btItemId = btItemId;

        this.exp = exp;
        this.gold = gold;
        this.rewardRankingPoint = rewardRankingPoint;
        this.looted = false;
    }
}

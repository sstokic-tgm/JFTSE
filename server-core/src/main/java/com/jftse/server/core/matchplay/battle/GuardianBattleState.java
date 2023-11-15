package com.jftse.server.core.matchplay.battle;

import com.jftse.entities.database.model.battle.BossGuardian;
import com.jftse.entities.database.model.battle.GuardianBase;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicBoolean;

@Getter
@Setter
public class GuardianBattleState extends BattleState {
    private final int id;
    private final int btItemId;
    private final boolean isBoss;

    private final int exp;
    private final int gold;
    private final int rewardRankingPoint;
    private AtomicBoolean looted;

    public GuardianBattleState(GuardianBase guardian, short position, int hp, int str, int sta, int dex, int will, int exp, int gold, int rewardRankingPoint) {
        super(position, hp, str, sta, dex, will);

        this.id = guardian.getId().intValue();
        this.btItemId = guardian.getBtItemID();
        this.isBoss = guardian instanceof BossGuardian;

        this.exp = exp;
        this.gold = gold;
        this.rewardRankingPoint = rewardRankingPoint;
        this.looted = new AtomicBoolean(false);
    }
}

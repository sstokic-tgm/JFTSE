package com.jftse.emulator.server.core.matchplay;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlayerReward {
    private int playerPosition;
    private int exp;
    private int gold;
    private int couplePoints;
    private int rankingPoints;
    @Deprecated
    private int productIndex;
    @Deprecated
    private int productAmount;
    private int activeBonuses;

    public PlayerReward(int playerPosition) {
        this.playerPosition = playerPosition;
        this.exp = 0;
        this.gold = 0;
        this.couplePoints = 0;
        this.rankingPoints = 0;
        this.productIndex = 0;
        this.productAmount = 0;
        this.activeBonuses = 0;
    }
}

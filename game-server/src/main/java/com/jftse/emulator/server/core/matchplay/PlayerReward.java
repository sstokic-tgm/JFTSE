package com.jftse.emulator.server.core.matchplay;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlayerReward {
    private int playerPosition;
    private int rewardExp;
    private int rewardGold;
    private int rewardRP;
    private int rewardProductIndex;
    private int productRewardAmount;
    private int activeBonuses = 0;
}

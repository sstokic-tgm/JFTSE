package com.jftse.emulator.server.game.core.matchplay;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlayerReward {
    private int playerPosition;
    private int basicRewardExp;
    private int basicRewardGold;
    private int rewardProductIndex;
    private int productRewardAmount;
}

package com.jftse.emulator.server.core.life.housing;

import lombok.Getter;
import lombok.Setter;

import java.util.Calendar;

@Getter
@Setter
public class FruitTree {
    private final short x;
    private final short y;

    private FruitReward fruitReward;

    private Calendar lastFruitPickTime;
    private int availableFruits;

    public FruitTree(short x, short y) {
        this.x = x;
        this.y = y;
        this.availableFruits = FruitManager.MAXIMUM_FRUITS_PER_TREE;
    }
}

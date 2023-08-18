package com.jftse.emulator.server.core.life.housing;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FruitTree {
    private final short x;
    private final short y;

    private FruitReward fruitReward;

    public FruitTree(short x, short y) {
        this.x = x;
        this.y = y;
    }
}

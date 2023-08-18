package com.jftse.emulator.server.core.life.housing;

import com.jftse.entities.database.model.item.ItemMaterial;
import lombok.Getter;

@Getter
public class FruitReward {
    private final ItemMaterial item;
    private final int quantity;

    public FruitReward(ItemMaterial item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }
}

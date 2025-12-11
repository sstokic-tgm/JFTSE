package com.jftse.emulator.server.core.life.housing;

import com.jftse.entities.database.model.item.ItemMaterial;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemProbability {
    private final int score;
    private final int probability;
    private final ItemMaterial item;
    private int quantity;

    public ItemProbability(int score, int probability, ItemMaterial item) {
        this.score = score;
        this.probability = probability;
        this.item = item;
    }
}

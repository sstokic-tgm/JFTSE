package com.jftse.emulator.server.core.life.housing;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class FruitDrop {
    private final int song;
    private final int score;
    private final int probability;
    private final int itemCount;
    private final List<ItemProbability> itemProbabilities;

    public FruitDrop(int song, int score, int probability, int itemCount) {
        this.song = song;
        this.score = score;
        this.probability = probability;
        this.itemCount = itemCount;
        this.itemProbabilities = new ArrayList<>();
    }

    public void addProbability(ItemProbability probability) {
        itemProbabilities.add(probability);
    }
}

package com.jftse.emulator.server.core.life.item;

import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.database.model.pocket.Pocket;

public abstract class AbstractItem<TResult> {
    private final int itemIndex;
    private final String name;
    private final String category;

    protected AbstractItem(final int itemIndex, final String name, final String category) {
        this.itemIndex = itemIndex;
        this.name = name;
        this.category = category;
    }

    public final int getItemIndex() {
        return itemIndex;
    }

    public final String getName() {
        return name;
    }

    public final String getCategory() {
        return category;
    }

    public abstract boolean processPlayer(Player player);
    public abstract boolean processPocket(Pocket pocket);

    public abstract TResult getResult();
}

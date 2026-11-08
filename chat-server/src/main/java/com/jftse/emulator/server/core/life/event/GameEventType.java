package com.jftse.emulator.server.core.life.event;

public enum GameEventType {
    ON_TICK,
    ON_LOGIN,
    ON_LOGOUT,

    SHOP_ITEM_BOUGHT,
    INVENTORY_ITEM_SOLD,

    RECIPE_COMBINED,

    ON_ENCHANT,

    GACHA_OPENED,

    TREE_SHAKE_SUCCESS,
    FISHING_SUCCESS,

    LOBBY_JOINED;

    public String getName() {
        return toString();
    }
}


package com.jftse.emulator.server.game.core.item;

public enum EItemRecipe {
    CHAR_ITEM(0), QUICK_SLOT(1), PET_ITEM(2), ENCHANT_ITEM(3), ETC_ITEM(4);

    private final byte value;

    EItemRecipe(int value) {
        this.value = (byte) value;
    }

    public Byte getValue() {
        return value;
    }

    public String getName() {
        return toString();
    }

    public static String getNameByValue(byte value) {
        for (EItemRecipe itemRecipe : values()) {
            if (itemRecipe.getValue().equals(value)) {
                return itemRecipe.getName();
            }
        }
        return null;
    }

    public static EItemRecipe valueOf(byte value) {
        for (EItemRecipe itemRecipe : values()) {
            if (itemRecipe.getValue().equals(value)) {
                return itemRecipe;
            }
        }
        return null;
    }
}

package com.jftse.emulator.server.game.core.item;

public enum EItemCategory {
    CHAR(0), PARTS(1), SPECIAL(2), QUICK(3), CARD(4), TOOL(5),
    PET_CHAR(6), HOUSE_DECO(7), MATERIAL(8), RECIPE(9), PET_ITEM(10),
    HOUSE(11), SKILL(12), LOTTERY(13), GUILD(14), ENCHANT(15),
    BALL(16);

    private final byte value;

    EItemCategory(int value) {
        this.value = (byte) value;
    }

    public Byte getValue() {
        return value;
    }

    public String getName() {
        return toString();
    }

    public static String getNameByValue(byte value) {
        for (EItemCategory itemCategory : values()) {
            if (itemCategory.getValue().equals(value)) {
                return itemCategory.getName();
            }
        }
        return null;
    }

    public static EItemCategory valueOf(byte value) {
        for (EItemCategory itemCategory : values()) {
            if (itemCategory.getValue().equals(value)) {
                return itemCategory;
            }
        }
        return null;
    }
}

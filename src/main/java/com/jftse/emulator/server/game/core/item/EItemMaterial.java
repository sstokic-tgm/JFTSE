package com.jftse.emulator.server.game.core.item;

public enum EItemMaterial {
    FISH(0), PLANT(1), BOOTY(2), LOCAL(3), ETC(4);

    private final byte value;

    EItemMaterial(int value) {
        this.value = (byte) value;
    }

    public Byte getValue() {
        return value;
    }

    public String getName() {
        return toString();
    }

    public static String getNameByValue(byte value) {
        for (EItemMaterial itemMaterial : values()) {
            if (itemMaterial.getValue().equals(value)) {
                return itemMaterial.getName();
            }
        }
        return null;
    }

    public static EItemMaterial valueOf(byte value) {
        for (EItemMaterial itemMaterial : values()) {
            if (itemMaterial.getValue().equals(value)) {
                return itemMaterial;
            }
        }
        return null;
    }
}

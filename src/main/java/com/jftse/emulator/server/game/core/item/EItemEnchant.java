package com.jftse.emulator.server.game.core.item;

public enum EItemEnchant {
    JEWEL(0), ELEMENTAL(1);

    private final byte value;

    EItemEnchant(int value) {
        this.value = (byte) value;
    }

    public Byte getValue() {
        return value;
    }

    public String getName() {
        return toString();
    }

    public static String getNameByValue(byte value) {
        for (EItemEnchant itemEnchant : values()) {
            if (itemEnchant.getValue().equals(value)) {
                return itemEnchant.getName();
            }
        }
        return null;
    }

    public static EItemEnchant valueOf(byte value) {
        for (EItemEnchant itemEnchant : values()) {
            if (itemEnchant.getValue().equals(value)) {
                return itemEnchant;
            }
        }
        return null;
    }
}

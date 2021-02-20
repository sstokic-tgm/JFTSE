package com.jftse.emulator.server.game.core.item;

public enum EItemHouseDeco {
    FURNITURE(0), DECO(1);

    private final byte value;

    EItemHouseDeco(int value) {
        this.value = (byte) value;
    }

    public Byte getValue() {
        return value;
    }

    public String getName() {
        return toString();
    }

    public static String getNameByValue(byte value) {
        for (EItemHouseDeco itemHouseDeco : values()) {
            if (itemHouseDeco.getValue().equals(value)) {
                return itemHouseDeco.getName();
            }
        }
        return null;
    }

    public static EItemHouseDeco valueOf(byte value) {
        for (EItemHouseDeco itemHouseDeco : values()) {
            if (itemHouseDeco.getValue().equals(value)) {
                return itemHouseDeco;
            }
        }
        return null;
    }
}

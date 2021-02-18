package com.jftse.emulator.server.game.core.item;

public enum EItemPart {
    SET(0), HEAD(1), UPPER(2), LOWER(3), FOOT(4), AUX(5), RACKET(6);

    private final byte value;

    EItemPart(int value) {
        this.value = (byte) value;
    }

    public Byte getValue() {
        return value;
    }

    public String getName() {
        return toString();
    }

    public static String getNameByValue(byte value) {
        for (EItemPart itemPart : values()) {
            if (itemPart.getValue().equals(value)) {
                return itemPart.getName();
            }
        }
        return null;
    }

    public static EItemPart valueOf(byte value) {
        for (EItemPart itemPart : values()) {
            if (itemPart.getValue().equals(value)) {
                return itemPart;
            }
        }
        return null;
    }
}

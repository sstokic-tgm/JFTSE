package com.jftse.emulator.server.game.core.item;

public enum EItemChar {
    ALL_CHAR(-1), NIKI(0), LUNLUN(1), DHANPIR(2), LUCY(3), SHUA(4), POCHI(5), AL(6);

    private final byte value;

    EItemChar(int value) {
        this.value = (byte) value;
    }

    public Byte getValue() {
        return value;
    }

    public String getName() {
        return toString();
    }

    public static String getNameByValue(byte value) {
        for (EItemChar itemChar : values()) {
            if (itemChar.getValue().equals(value)) {
                return itemChar.getName();
            }
        }
        return null;
    }

    public static EItemChar valueOf(byte value) {
        for (EItemChar itemChar : values()) {
            if (itemChar.getValue().equals(value)) {
                return itemChar;
            }
        }
        return null;
    }
}

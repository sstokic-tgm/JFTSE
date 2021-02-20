package com.jftse.emulator.server.game.core.item;

import java.util.ArrayList;
import java.util.List;

public enum EItemSubPart {

    DYE(1), HAIR(1), CAP(1),
    BODY(2),
    PANTS(3),
    SOCKS(4), FOOT(4),
    BAG(5), GLASSES(5), HAND(5),
    RACKET(6);

    private final byte value;

    EItemSubPart(int value) {
        this.value = (byte) value;
    }

    public Byte getValue() {
        return value;
    }

    public String getName() {
        return toString();
    }

    public static String getNameByValue(byte value) {
        for (EItemSubPart itemSubPart : values()) {
            if (itemSubPart.getValue().equals(value)) {
                return itemSubPart.getName();
            }
        }
        return null;
    }

    public static EItemSubPart valueOf(byte value) {
        for (EItemSubPart itemSubPart : values()) {
            if (itemSubPart.getValue().equals(value)) {
                return itemSubPart;
            }
        }
        return null;
    }

    public static List<String> getNamesByValue(byte value) {
        List<String> result = new ArrayList<>();
        for (EItemSubPart itemSubPart : values()) {
            if (itemSubPart.getValue().equals(value)) {
                result.add(itemSubPart.getName());
            }
        }
        return result;
    }
}

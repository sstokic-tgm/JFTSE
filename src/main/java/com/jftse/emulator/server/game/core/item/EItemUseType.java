package com.jftse.emulator.server.game.core.item;

import org.apache.commons.lang3.StringUtils;

public enum EItemUseType {

    TIME(1), COUNT(2), DURABLE(3), INSTANT(4);

    private byte value;

    EItemUseType(int value) {
        this.value = (byte) value;
    }

    public Byte getValue() {
        return value;
    }

    public String getName() {
        return StringUtils.capitalize(toString().toLowerCase());
    }

    public static EItemUseType valueOf(byte value) {
        for (EItemUseType itemUseType : values()) {
            if (itemUseType.getValue().equals(value)) {
                return itemUseType;
            }
        }
        return null;
    }

    public static Byte getValueByName(String name) {
        for (EItemUseType itemUseType : values()) {
            if (itemUseType.getName().equals(name)) {
                return itemUseType.getValue();
            }
        }
        return null;
    }
}

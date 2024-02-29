package com.jftse.server.core.item;

import org.apache.commons.lang3.StringUtils;

public enum EElementalKind {
    NONE(0), JEWEL(0), STR(1), STA(2), DEX(3), WIS(4), EARTH(5), WIND(6), WATER(7), FIRE(8);

    private final int value;

    EElementalKind(int value) {
        this.value = value;
    }

    public byte getValue() {
        return (byte) value;
    }

    public String getName() {
        return toString();
    }

    public String getNameNormalized() {
        return StringUtils.capitalize(toString().toLowerCase());
    }
}

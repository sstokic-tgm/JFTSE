package com.jftse.server.core.item;

public enum EElementalProperty {
    NONE(0, EElementalKind.NONE), EARTH(1, EElementalKind.EARTH), WIND(2, EElementalKind.WIND), FIRE(3, EElementalKind.FIRE), WATER(4, EElementalKind.WATER);

    private final int value;
    private final EElementalKind kind;

    EElementalProperty(int value, EElementalKind kind) {
        this.value = value;
        this.kind = kind;
    }

    public byte getValue() {
        return (byte) value;
    }

    public EElementalKind getKind() {
        return kind;
    }

    public String getName() {
        return toString();
    }

    public static EElementalProperty fromValue(byte value) {
        return switch (value) {
            case 1 -> EARTH;
            case 2 -> WIND;
            case 3 -> FIRE;
            case 4 -> WATER;
            default -> NONE;
        };
    }

    public static EElementalProperty fromKind(EElementalKind kind) {
        return switch (kind) {
            case EARTH -> EARTH;
            case WIND -> WIND;
            case FIRE -> FIRE;
            case WATER -> WATER;
            default -> NONE;
        };
    }

    public static EElementalProperty fromName(String name) {
        return switch (name.toUpperCase()) {
            case "EARTH" -> EARTH;
            case "WIND" -> WIND;
            case "FIRE" -> FIRE;
            case "WATER" -> WATER;
            default -> NONE;
        };
    }

    public static EElementalProperty fromKindName(String kindName) {
        return switch (kindName.toUpperCase()) {
            case "EARTH" -> EARTH;
            case "WIND" -> WIND;
            case "FIRE" -> FIRE;
            case "WATER" -> WATER;
            default -> NONE;
        };
    }
}

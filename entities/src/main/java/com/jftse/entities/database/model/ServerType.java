package com.jftse.entities.database.model;

public enum ServerType {
    NONE(-1), AUTH_SERVER(0), GAME_SERVER(1), CHAT_SERVER(4), RELAY_SERVER(2), AC_SERVER(10);

    private final int value;

    ServerType(int value) {
        this.value = value;
    }

    public String getName() {
        return toString();
    }

    public int getValue() {
        return value;
    }

    public static ServerType fromValue(int value) {
        for (ServerType type : ServerType.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return null;
    }

    public static ServerType fromName(String name) {
        for (ServerType type : ServerType.values()) {
            if (type.getName().equals(name)) {
                return type;
            }
        }
        return null;
    }
}

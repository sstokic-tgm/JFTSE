package com.jftse.entities.database.model;

public enum ServerType {
    AUTH_SERVER, GAME_SERVER, RELAY_SERVER, AC_SERVER;

    public String getName() {
        return toString();
    }
}

package com.jftse.emulator.server.core.life.event;

public enum GameEventType {
    ON_TICK,
    ON_LOGIN,
    ON_LOGOUT;

    public String getName() {
        return toString();
    }
}

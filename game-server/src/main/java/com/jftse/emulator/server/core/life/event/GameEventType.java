package com.jftse.emulator.server.core.life.event;

public enum GameEventType {
    GAME, MATCH_START, MATCH_END, ON_LOGIN;

    public String getName() {
        return toString();
    }
}

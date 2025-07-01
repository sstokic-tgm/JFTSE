package com.jftse.emulator.server.core.life.event;

@FunctionalInterface
public interface GameEventCallback {
    void onEvent(Object... args);
}

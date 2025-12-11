package com.jftse.emulator.server.core.life.housing;

import lombok.Getter;

@Getter
public enum FishState {
    IDLE(0),
    MOVING(1),
    ATTACKING(2),
    FRIGHTENED(3),
    BITING(4),
    CAUGHT(5),
    ODD(6);

    private final int value;

    FishState(int value) {
        this.value = value;
    }

    public static FishState fromValue(int value) {
        for (FishState state : values()) {
            if (state.getValue() == value) {
                return state;
            }
        }
        return ODD;
    }
}

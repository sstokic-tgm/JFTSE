package com.jftse.entities.database.model.log;

public enum GameLogType {
    BASIC_GAME, BATTLE_GAME, GUARDIAN_GAME, GENERAL, GAME_TIME, BANABLE;

    public String getName() {
        return toString();
    }
}

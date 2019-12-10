package com.ft.emulator.server.game.matchplay.room;

import com.ft.emulator.server.database.model.character.CharacterPlayer;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomPlayer {

    private CharacterPlayer player;

    private Character position;
    private Boolean master;
    private Boolean ready;
    private Boolean fitting;
}
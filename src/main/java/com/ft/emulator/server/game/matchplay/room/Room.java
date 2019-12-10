package com.ft.emulator.server.game.matchplay.room;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Room {

    private Character id;
    private String name;
    private Byte gameMode;
    private Byte battleMode;
    private Boolean betting;
    private Byte bettingMode;
    private Byte bettingCoins;
    private Integer bettingGold;
    private Integer ball;
    private Byte maxPlayers;
    private Boolean isPrivate;
    private Byte level;
    private Byte levelRange;
    private Byte map;
    private List<RoomPlayer> playerList;

    public Room() {

        this.playerList = new ArrayList<>();
    }
}
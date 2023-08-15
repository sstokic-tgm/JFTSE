package com.jftse.emulator.server.core.life.room;

import com.jftse.emulator.server.core.constants.RoomStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class Room {
    public Room() {
        bannedPlayers = new ConcurrentLinkedDeque<>();
        roomPlayerList = new ConcurrentLinkedDeque<>();
        nextPlayerPosition = new AtomicInteger(0);
        status = RoomStatus.NotRunning;
    }

    private short roomId;
    private String roomName;
    private byte roomType;
    private byte allowBattlemon;
    private byte mode;
    private byte rule;
    private byte players;
    private boolean isPrivate;
    private boolean skillFree;
    private boolean quickSlot;
    private byte level;
    private byte levelRange;
    private char bettingType;
    private int bettingAmount;
    private byte map;
    private int ball;
    private String password;
    private ConcurrentLinkedDeque<Long> bannedPlayers;
    private ConcurrentLinkedDeque<RoomPlayer> roomPlayerList;
    private AtomicInteger nextPlayerPosition;
    private int status;

    // Guardian
    private boolean isHardMode; // Guardians are very strong
    private boolean isArcade; // You have to play against all guardians there are
    private boolean isRandomGuardians; // Always random guardians are spawned.
}

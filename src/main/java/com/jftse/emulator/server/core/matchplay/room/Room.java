package com.jftse.emulator.server.core.matchplay.room;

import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.core.constants.RoomPositionState;
import com.jftse.emulator.server.core.constants.RoomStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

@Getter
@Setter
public class Room {
    public Room() {
        bannedPlayers = new ConcurrentLinkedDeque<>();
        roomPlayerList = new ConcurrentLinkedDeque<>();
        positions = new ConcurrentLinkedDeque<>(Arrays.asList(
                RoomPositionState.Free, RoomPositionState.Free,
                RoomPositionState.Free, RoomPositionState.Free,
                RoomPositionState.Locked, RoomPositionState.Free,
                RoomPositionState.Free, RoomPositionState.Free,
                RoomPositionState.Free, RoomPositionState.Locked));
        status = RoomStatus.NotRunning;
    }

    private short roomId;
    private String roomName;
    private byte allowBattlemon;
    private byte mode;
    private byte rule;
    private byte players;
    private boolean isPrivate;
    private byte unk1;
    private boolean skillFree;
    private boolean quickSlot;
    private byte level;
    private byte levelRange;
    private char bettingType;
    private int bettingAmount;
    private byte map;
    private int ball;
    private String password;
    private ConcurrentLinkedDeque<Player> bannedPlayers;
    private ConcurrentLinkedDeque<RoomPlayer> roomPlayerList;
    private ConcurrentLinkedDeque<Short> positions;
    private int status;

    // Guardian
    private boolean isHardMode; // Guardians are very strong
    private boolean isArcade; // You have to play against all guardians there are
    private boolean isRandomGuardians; // Always random guardians are spawned.
}
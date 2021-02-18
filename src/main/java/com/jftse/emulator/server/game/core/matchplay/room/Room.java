package com.jftse.emulator.server.game.core.matchplay.room;

import com.jftse.emulator.server.game.core.constants.RoomPositionState;
import com.jftse.emulator.server.game.core.constants.RoomStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class Room {
    public Room() {
        roomPlayerList = new ArrayList<>();
        positions = Arrays.asList(
                RoomPositionState.Free, RoomPositionState.Free,
                RoomPositionState.Free, RoomPositionState.Free,
                RoomPositionState.Locked, RoomPositionState.Free,
                RoomPositionState.Free, RoomPositionState.Free,
                RoomPositionState.Free);
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
    private List<RoomPlayer> roomPlayerList;
    private List<Short> positions;
    private int status;
}
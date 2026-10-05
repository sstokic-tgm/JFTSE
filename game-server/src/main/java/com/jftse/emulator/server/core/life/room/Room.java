package com.jftse.emulator.server.core.life.room;

import com.jftse.emulator.server.core.constants.RoomPositionState;
import com.jftse.emulator.server.core.constants.RoomStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;

@Getter
@Setter
public class Room {
    public Room() {
        bannedPlayers = new ConcurrentLinkedDeque<>();
        invitedPlayerIds = new ConcurrentLinkedDeque<>();
        roomPlayerList = new ConcurrentLinkedDeque<>();
        positions = new ArrayList<>(Arrays.asList(
                RoomPositionState.Free, RoomPositionState.Free,
                RoomPositionState.Free, RoomPositionState.Free,
                RoomPositionState.Locked, RoomPositionState.Free,
                RoomPositionState.Free, RoomPositionState.Free,
                RoomPositionState.Free, RoomPositionState.Locked));
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
    private ConcurrentLinkedDeque<Long> invitedPlayerIds;
    private ConcurrentLinkedDeque<RoomPlayer> roomPlayerList;
    private ArrayList<Short> positions;
    private int status;

    private byte previousMap = 0;

    // Guardian
    private boolean isHardMode; // Guardians are very strong
    private boolean isArcade; // You have to play against all guardians there are
    private boolean isRandomGuardians; // Always random guardians are spawned.

    // Generic extension-point mechanism (see game-server/.../matchplay/extension/) - lets a plugin
    // register its own mode, identified by an arbitrary string id, without a dedicated boolean
    // field per mode on this class. See isModeActive/setModeActive below.
    private final Set<String> activeExtensionModes = new HashSet<>();

    public boolean isModeActive(String modeId) {
        return activeExtensionModes.contains(modeId);
    }

    public void setModeActive(String modeId, boolean active) {
        if (active) {
            activeExtensionModes.add(modeId);
        } else {
            activeExtensionModes.remove(modeId);
        }
    }
}

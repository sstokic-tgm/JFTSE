package com.jftse.emulator.server.core.matchplay.extension;

import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.entities.database.model.battle.GuardianBase;

import java.util.List;

// Consulted by MatchplayGuardianModeHandler#onPrepare/onStart - see PLUGIN_README.md for the
// bigger picture on how a plugin implements one of these.
public interface MatchplayLifecycleExtension {
    // Return null to defer to the core's default guardian-selection logic.
    default List<GuardianBase> overrideInitialGuardians(MatchplayGuardianGame game, Room room) {
        return null;
    }

    default void onMatchStarting(FTClient client, MatchplayGuardianGame game) {
    }

    // Consulted by RoomStartGamePacketHandler before a room's match is actually started (before
    // the room's status changes or a game session is created). Return null to allow the start, or
    // a rejection message to block it - the message is broadcast to the room as a chat message and
    // the start is aborted, leaving the room untouched.
    default String validateMatchStart(Room room) {
        return null;
    }
}

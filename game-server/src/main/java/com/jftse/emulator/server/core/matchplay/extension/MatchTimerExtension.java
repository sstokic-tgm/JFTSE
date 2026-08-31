package com.jftse.emulator.server.core.matchplay.extension;

import com.jftse.emulator.server.core.life.room.GameSession;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.net.FTConnection;

// Registered implementations are gathered by ServiceManager (Spring List<MatchTimerExtension>
// autowiring) and consulted by DefeatTimerTask before it falls back to the map's own
// playTime/bossPlayTime.
public interface MatchTimerExtension {
    boolean overridesTimer(MatchplayGuardianGame game);

    void applyTimer(FTConnection connection, GameSession gameSession, MatchplayGuardianGame game);
}

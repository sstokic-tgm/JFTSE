package com.jftse.emulator.server.core.matchplay.extension;

import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.net.FTConnection;

// Consulted by SpellHitsTargetHandler#handleAllGuardiansDead - see PLUGIN_README.md for the
// bigger picture on how a plugin implements one of these.
public interface WaveCompletionExtension {
    boolean handlesWaveCompletion(MatchplayGuardianGame game);

    void onAllGuardiansDead(FTConnection connection, MatchplayGuardianGame game);
}

package com.jftse.emulator.server.core.matchplay.extension;

import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.entities.database.model.battle.GuardianBase;
import com.jftse.server.core.matchplay.battle.GuardianBattleState;

import java.util.Optional;

// Consulted by MatchplayGuardianGame#createGuardianBattleState - see PLUGIN_README.md for the
// bigger picture on how a plugin implements one of these.
public interface GuardianBattleStateProvider {
    Optional<GuardianBattleState> tryCreate(MatchplayGuardianGame game, GuardianBase guardian, short guardianPosition, int activePlayingPlayersCount);
}

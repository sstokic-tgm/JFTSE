package com.jftse.emulator.server.core.matchplay.extension;

import com.jftse.emulator.server.core.matchplay.MatchplayReward;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;

// Consulted by MatchplayGuardianModeHandler#onEnd, after the default game.getMatchRewards() has
// built `reward` (item-reward slots etc. - untouched by this). Implement this to overwrite
// exp/gold/rankingPoints on reward's PlayerRewards with FINAL totals (bonuses already applied, by
// your own logic, however you see fit) that a plugin already granted to players itself over the
// course of the match (e.g. incrementally, floor by floor) - return true to have that. Doing so
// also suppresses MatchplayGuardianModeHandler's own addBonusesToRewards() (house/ring/wiseman),
// its normal re-application of exp/gold to player accounts, and its classic per-guardian
// ranking-point calculation, since your plugin's own totals are assumed complete (rankingPoints
// is the one exception that still gets applied to the account here, exactly once, since a rating
// update - unlike exp/gold - only makes sense once per match). Return false to leave the default
// exp/gold/rankingPoints in place - the default calculation, addBonusesToRewards, and the account
// grant all run as usual.
public interface MatchRewardExtension {
    boolean tryOverrideMatchRewardTotals(MatchplayGuardianGame game, MatchplayReward reward);
}

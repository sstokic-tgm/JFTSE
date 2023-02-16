package com.jftse.emulator.server.core.matchplay.combat;

import com.jftse.emulator.common.exception.ValidationException;
import com.jftse.emulator.server.core.matchplay.battle.GuardianBattleState;
import com.jftse.emulator.server.core.matchplay.battle.PlayerBattleState;

public interface GuardianCombatable extends Combat {
    short dealDamageToPlayer(int attackerPos, int targetPos, short damage, boolean hasAttackerDmgBuff, boolean hasTargetDefBuff) throws ValidationException;
    short dealDamageOnBallLossToPlayer(int attackerPos, int targetPos, boolean hasAttackerWillBuff) throws ValidationException;
    short updateHealthByDamage(GuardianBattleState targetGuardian, int dmg);
    short updateHealthByDamage(PlayerBattleState targetPlayer, int dmg);
    GuardianBattleState reviveAnyGuardian(short revivePercentage) throws ValidationException;
}

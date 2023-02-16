package com.jftse.emulator.server.core.matchplay.combat;

import com.jftse.emulator.common.exception.ValidationException;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.server.core.matchplay.battle.PlayerBattleState;

public interface PlayerCombatable extends Combat {
    short updateHealthByDamage(PlayerBattleState targetPlayer, int dmg);
    PlayerBattleState reviveAnyPlayer(short revivePercentage, RoomPlayer roomPlayer) throws ValidationException;
    PlayerBattleState reviveAnyPlayer(short revivePercentage) throws ValidationException;
    short getPlayerCurrentHealth(short playerPos) throws ValidationException;
}

package com.jftse.emulator.server.core.matchplay.combat;

import com.jftse.emulator.common.exception.ValidationException;
import com.jftse.emulator.server.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.core.matchplay.game.MatchplayBattleGame;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.core.matchplay.battle.PlayerBattleState;
import com.jftse.emulator.server.core.matchplay.room.RoomPlayer;
import com.jftse.emulator.server.core.utils.BattleUtils;
import com.jftse.emulator.server.database.model.battle.WillDamage;

public class PlayerCombatSystem implements PlayerCombatable {
    private MatchplayGame game;

    private final boolean isBattleGame;

    public PlayerCombatSystem(MatchplayGame game) {
        this.game = game;

        isBattleGame = game instanceof MatchplayBattleGame;
    }

    @Override
    public short dealDamage(int attackerPos, int targetPos, short damage, boolean hasAttackerDmgBuff, boolean hasTargetDefBuff) throws ValidationException {
        int totalDamageToDeal = damage;
        PlayerBattleState attackingPlayer = isBattleGame ?
                ((MatchplayBattleGame) game).getPlayerBattleStates().stream()
                        .filter(x -> x.getPosition().get() == attackerPos)
                        .findFirst()
                        .orElse(null) :
                ((MatchplayGuardianGame) game).getPlayerBattleStates().stream()
                        .filter(x -> x.getPosition().get() == attackerPos)
                        .findFirst()
                        .orElse(null);

        boolean isNormalDamageSkill = Math.abs(damage) != 1;
        if (isNormalDamageSkill) {
            totalDamageToDeal = BattleUtils.calculateDmg(attackingPlayer.getStr().get(), damage, hasAttackerDmgBuff);
        }

        PlayerBattleState targetPlayer = isBattleGame ?
                ((MatchplayBattleGame) game).getPlayerBattleStates().stream()
                        .filter(x -> x.getPosition().get() == targetPos)
                        .findFirst()
                        .orElse(null) :
                ((MatchplayGuardianGame) game).getPlayerBattleStates().stream()
                        .filter(x -> x.getPosition().get() == targetPos)
                        .findFirst()
                        .orElse(null);

        if (targetPlayer == null)
            throw new ValidationException("targetPlayer battle state is null");

        if (isNormalDamageSkill) {
            int damageToDeny = BattleUtils.calculateDef(targetPlayer.getSta().get(), Math.abs(totalDamageToDeal), hasTargetDefBuff);
            if (damageToDeny > Math.abs(totalDamageToDeal)) {
                totalDamageToDeal = -1;
            } else {
                totalDamageToDeal += damageToDeny;
            }
        }

        return updateHealthByDamage(targetPlayer, totalDamageToDeal);
    }

    @Override
    public short dealDamageOnBallLoss(int attackerPos, int targetPos, boolean hasAttackerWillBuff) throws ValidationException {
        PlayerBattleState targetPlayer = isBattleGame ?
                ((MatchplayBattleGame) game).getPlayerBattleStates().stream()
                        .filter(x -> x.getPosition().get() == targetPos)
                        .findFirst()
                        .orElse(null) :
                ((MatchplayGuardianGame) game).getPlayerBattleStates().stream()
                        .filter(x -> x.getPosition().get() == targetPos)
                        .findFirst()
                        .orElse(null);

        if (targetPlayer == null)
            throw new ValidationException("targetPlayer battle state is null");

        int lossBallDamage = 0;
        boolean servingGuardianScored = attackerPos == 4;
        if (servingGuardianScored) {
            lossBallDamage = (short) -(targetPlayer.getMaxHealth().get() * 0.1);
        } else {
            PlayerBattleState attackingPlayer = isBattleGame ?
                    ((MatchplayBattleGame) game).getPlayerBattleStates().stream()
                            .filter(x -> x.getPosition().get() == attackerPos)
                            .findFirst()
                            .orElse(null) :
                    ((MatchplayGuardianGame) game).getPlayerBattleStates().stream()
                            .filter(x -> x.getPosition().get() == attackerPos)
                            .findFirst()
                            .orElse(null);

            if (attackingPlayer != null) {
                WillDamage willDamage = game.getWillDamages().stream()
                        .filter(x -> x.getWill() == attackingPlayer.getWill().get())
                        .findFirst()
                        .orElse(game.getWillDamages().get(game.getWillDamages().size() - 1));
                lossBallDamage = -BattleUtils.calculateBallDamageByWill(willDamage, hasAttackerWillBuff);
            }
        }

        return updateHealthByDamage(targetPlayer, lossBallDamage);
    }

    @Override
    public short heal(int targetPos, short percentage) throws ValidationException {
        PlayerBattleState targetPlayer = isBattleGame ?
                ((MatchplayBattleGame) game).getPlayerBattleStates().stream()
                        .filter(x -> x.getPosition().get() == targetPos)
                        .findFirst()
                        .orElse(null) :
                ((MatchplayGuardianGame) game).getPlayerBattleStates().stream()
                        .filter(x -> x.getPosition().get() == targetPos)
                        .findFirst()
                        .orElse(null);

        if (targetPlayer == null)
            throw new ValidationException("targetPlayer battle state is null");

        short healthToHeal = (short) (targetPlayer.getMaxHealth().get() * (percentage / 100f));
        short currentHealth = (short) Math.max(targetPlayer.getCurrentHealth().get(), 0);
        short newPlayerHealth = (short) (currentHealth + healthToHeal);
        if (newPlayerHealth > targetPlayer.getMaxHealth().get()) {
            newPlayerHealth = (short) targetPlayer.getMaxHealth().get();
        }

        targetPlayer.getCurrentHealth().getAndSet(newPlayerHealth);
        return newPlayerHealth;
    }

    @Override
    public short updateHealthByDamage(PlayerBattleState targetPlayer, int dmg) {
        short newPlayerHealth = (short) (targetPlayer.getCurrentHealth().get() + dmg);
        if (newPlayerHealth < 1) {
            targetPlayer.getDead().getAndSet(true);
        }

        newPlayerHealth = newPlayerHealth < 0 ? 0 : newPlayerHealth;
        targetPlayer.getCurrentHealth().getAndSet(newPlayerHealth);
        return newPlayerHealth;
    }

    @Override
    public PlayerBattleState reviveAnyPlayer(short revivePercentage, RoomPlayer roomPlayer) throws ValidationException {
        if (roomPlayer != null) {
            boolean isRedTeam = game.isRedTeam(roomPlayer.getPosition());

            PlayerBattleState playerBattleState = isBattleGame ?
                    ((MatchplayBattleGame) game).getPlayerBattleStates().stream()
                            .filter(x -> isRedTeam == game.isRedTeam(x.getPosition().get()) && x.getDead().get()
                                    || !isRedTeam == !game.isRedTeam(x.getPosition().get()) && x.getDead().get())
                            .findFirst()
                            .orElse(null) :
                    ((MatchplayGuardianGame) game).getPlayerBattleStates().stream()
                            .filter(x -> isRedTeam == game.isRedTeam(x.getPosition().get()) && x.getDead().get()
                                    || !isRedTeam == !game.isRedTeam(x.getPosition().get()) && x.getDead().get())
                            .findFirst()
                            .orElse(null);

            if (playerBattleState != null) {
                short newPlayerHealth = heal(playerBattleState.getPosition().get(), revivePercentage);
                playerBattleState.getCurrentHealth().getAndSet(newPlayerHealth);
                playerBattleState.getDead().getAndSet(false);
            }
            return playerBattleState;
        }
        return null;
    }

    @Override
    public PlayerBattleState reviveAnyPlayer(short revivePercentage) throws ValidationException {
        PlayerBattleState playerBattleState = isBattleGame ?
                ((MatchplayBattleGame) game).getPlayerBattleStates().stream()
                        .filter(x -> x.getDead().get())
                        .findFirst()
                        .orElse(null) :
                ((MatchplayGuardianGame) game).getPlayerBattleStates().stream()
                        .filter(x -> x.getDead().get())
                        .findFirst()
                        .orElse(null);

        if (playerBattleState != null) {
            short newPlayerHealth = heal(playerBattleState.getPosition().get(), revivePercentage);
            playerBattleState.getCurrentHealth().getAndSet(newPlayerHealth);
            playerBattleState.getDead().getAndSet(false);
        }

        return playerBattleState;
    }

    @Override
    public short getPlayerCurrentHealth(short playerPos) throws ValidationException {
        PlayerBattleState playerBattleState = isBattleGame ?
                ((MatchplayBattleGame) game).getPlayerBattleStates().stream()
                        .filter(x -> x.getPosition().get() == playerPos)
                        .findFirst()
                        .orElse(null) :
                ((MatchplayGuardianGame) game).getPlayerBattleStates().stream()
                        .filter(x -> x.getPosition().get() == playerPos)
                        .findFirst()
                        .orElse(null);

        if (playerBattleState == null)
            throw new ValidationException("playerBattleState is null");

        return (short) playerBattleState.getCurrentHealth().get();
    }
}

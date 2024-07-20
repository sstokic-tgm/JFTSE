package com.jftse.emulator.server.core.matchplay.combat;

import com.jftse.emulator.common.exception.ValidationException;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.core.matchplay.game.MatchplayBattleGame;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.core.utils.BattleUtils;
import com.jftse.entities.database.model.battle.Skill;
import com.jftse.entities.database.model.battle.WillDamage;
import com.jftse.server.core.item.EElementalProperty;
import com.jftse.server.core.matchplay.Elementable;
import com.jftse.server.core.matchplay.battle.BattleState;
import com.jftse.server.core.matchplay.battle.PlayerBattleState;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
public class PlayerCombatSystem implements PlayerCombatable {
    private MatchplayGame game;

    private final boolean isBattleGame;

    public PlayerCombatSystem(MatchplayGame game) {
        this.game = game;

        isBattleGame = game instanceof MatchplayBattleGame;
    }

    @Override
    public short dealDamage(int attackerPos, int targetPos, short damage, boolean hasAttackerDmgBuff, boolean hasTargetDefBuff, Skill skill) throws ValidationException {
        int totalDamageToDeal = damage;
        PlayerBattleState attackingPlayer = isBattleGame ?
                ((MatchplayBattleGame) game).getPlayerBattleStates().stream()
                        .filter(x -> x.getPosition() == attackerPos)
                        .findFirst()
                        .orElse(null) :
                ((MatchplayGuardianGame) game).getPlayerBattleStates().stream()
                        .filter(x -> x.getPosition() == attackerPos)
                        .findFirst()
                        .orElse(null);

        boolean isNormalDamageSkill = Math.abs(damage) != 1;
        if (isNormalDamageSkill) {
            totalDamageToDeal = BattleUtils.calculateDmg(attackingPlayer.getStr(), damage, hasAttackerDmgBuff);
        }

        PlayerBattleState targetPlayer = isBattleGame ?
                ((MatchplayBattleGame) game).getPlayerBattleStates().stream()
                        .filter(x -> x.getPosition() == targetPos)
                        .findFirst()
                        .orElse(null) :
                ((MatchplayGuardianGame) game).getPlayerBattleStates().stream()
                        .filter(x -> x.getPosition() == targetPos)
                        .findFirst()
                        .orElse(null);

        if (targetPlayer == null)
            throw new ValidationException("targetPlayer battle state is null");

        if (isNormalDamageSkill) {
            int damageToDeny = BattleUtils.calculateDef(targetPlayer.getSta(), Math.abs(totalDamageToDeal), hasTargetDefBuff);
            if (damageToDeny > Math.abs(totalDamageToDeal)) {
                totalDamageToDeal = -1;
            } else {
                totalDamageToDeal += damageToDeny;
            }

            Elementable offensiveElement = attackingPlayer.getOffensiveElement();

            if (totalDamageToDeal != -1 && offensiveElement != null && skill != null && offensiveElement.getProperty() == EElementalProperty.fromValue(skill.getElemental().byteValue())) {
                double efficiency = offensiveElement.getEfficiency();

                List<Elementable> defensiveElements = targetPlayer.getDefensiveElements();
                for (Elementable defensiveElement : defensiveElements) {
                    if (offensiveElement.isStrongAgainst(defensiveElement)) {
                        efficiency += 26;
                    } else if (offensiveElement.isWeakAgainst(defensiveElement)) {
                        efficiency -= 15;
                    } else if (defensiveElement.isResistantTo(offensiveElement)) {
                        efficiency -= 20;
                    }
                }

                final double efficiencyMultiplier = 1 + (efficiency / 100.0);
                totalDamageToDeal =  (int) (totalDamageToDeal * efficiencyMultiplier);
            }
        }

        return updateHealthByDamage(targetPlayer, totalDamageToDeal);
    }

    @Override
    public short dealDamageOnBallLoss(int attackerPos, int targetPos, boolean hasAttackerWillBuff) throws ValidationException {
        PlayerBattleState targetPlayer = isBattleGame ?
                ((MatchplayBattleGame) game).getPlayerBattleStates().stream()
                        .filter(x -> x.getPosition() == targetPos)
                        .findFirst()
                        .orElse(null) :
                ((MatchplayGuardianGame) game).getPlayerBattleStates().stream()
                        .filter(x -> x.getPosition() == targetPos)
                        .findFirst()
                        .orElse(null);

        if (targetPlayer == null)
            throw new ValidationException("targetPlayer battle state is null");

        int lossBallDamage = 0;
        boolean servingGuardianScored = attackerPos == 4;
        if (servingGuardianScored) {
            lossBallDamage = (short) -(targetPlayer.getMaxHealth() * 0.1);
        } else {
            PlayerBattleState attackingPlayer = isBattleGame ?
                    ((MatchplayBattleGame) game).getPlayerBattleStates().stream()
                            .filter(x -> x.getPosition() == attackerPos)
                            .findFirst()
                            .orElse(null) :
                    ((MatchplayGuardianGame) game).getPlayerBattleStates().stream()
                            .filter(x -> x.getPosition() == attackerPos)
                            .findFirst()
                            .orElse(null);

            if (attackingPlayer != null) {
                WillDamage willDamage = game.getWillDamages().stream()
                        .filter(x -> x.getWill() == attackingPlayer.getWill())
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
                        .filter(x -> x.getPosition() == targetPos)
                        .findFirst()
                        .orElse(null) :
                ((MatchplayGuardianGame) game).getPlayerBattleStates().stream()
                        .filter(x -> x.getPosition() == targetPos)
                        .findFirst()
                        .orElse(null);

        if (targetPlayer == null)
            throw new ValidationException("targetPlayer battle state is null");

        int currentHealth = targetPlayer.getCurrentHealth().get();
        short healthToHeal = (short) (targetPlayer.getMaxHealth() * (percentage / 100f));
        currentHealth = Math.max(currentHealth, 0);
        short newPlayerHealth = (short) (currentHealth + healthToHeal);
        if (newPlayerHealth > targetPlayer.getMaxHealth()) {
            newPlayerHealth = (short) targetPlayer.getMaxHealth();
        }

        if (targetPlayer.getCurrentHealth().compareAndSet(currentHealth, newPlayerHealth))
            return newPlayerHealth;
        else
            return (short) currentHealth;
    }

    @Override
    public short updateHealthByDamage(PlayerBattleState targetPlayer, int dmg) {
        int currentHealth = targetPlayer.getCurrentHealth().get();
        currentHealth = Math.max(currentHealth, 0);
        short newPlayerHealth = (short) (currentHealth + dmg);
        if (newPlayerHealth < 1) {
            targetPlayer.setDead(true);
        }
        newPlayerHealth = newPlayerHealth < 0 ? 0 : newPlayerHealth;

        if (targetPlayer.getCurrentHealth().compareAndSet(currentHealth, newPlayerHealth))
            return newPlayerHealth;
        else
            return (short) currentHealth;
    }

    @Override
    public PlayerBattleState reviveAnyPlayer(short revivePercentage, RoomPlayer roomPlayer) throws ValidationException {
        if (roomPlayer != null) {
            boolean isRedTeam = game.isRedTeam(roomPlayer.getPosition());

            PlayerBattleState playerBattleState = isBattleGame ?
                    ((MatchplayBattleGame) game).getPlayerBattleStates().stream()
                            .filter(x -> isRedTeam == game.isRedTeam(x.getPosition()) && x.isDead()
                                    || !isRedTeam == !game.isRedTeam(x.getPosition()) && x.isDead())
                            .findFirst()
                            .orElse(null) :
                    ((MatchplayGuardianGame) game).getPlayerBattleStates().stream()
                            .filter(x -> isRedTeam == game.isRedTeam(x.getPosition()) && x.isDead()
                                    || !isRedTeam == !game.isRedTeam(x.getPosition()) && x.isDead())
                            .findFirst()
                            .orElse(null);

            if (playerBattleState != null) {
                heal(playerBattleState.getPosition(), revivePercentage);
                playerBattleState.setDead(false);
            }
            return playerBattleState;
        }
        return null;
    }

    @Override
    public PlayerBattleState reviveAnyPlayer(short revivePercentage) throws ValidationException {
        PlayerBattleState playerBattleState = isBattleGame ?
                ((MatchplayBattleGame) game).getPlayerBattleStates().stream()
                        .filter(BattleState::isDead)
                        .findFirst()
                        .orElse(null) :
                ((MatchplayGuardianGame) game).getPlayerBattleStates().stream()
                        .filter(BattleState::isDead)
                        .findFirst()
                        .orElse(null);

        if (playerBattleState != null) {
            heal(playerBattleState.getPosition(), revivePercentage);
            playerBattleState.setDead(false);
        }

        return playerBattleState;
    }

    @Override
    public short getPlayerCurrentHealth(short playerPos) throws ValidationException {
        PlayerBattleState playerBattleState = isBattleGame ?
                ((MatchplayBattleGame) game).getPlayerBattleStates().stream()
                        .filter(x -> x.getPosition() == playerPos)
                        .findFirst()
                        .orElse(null) :
                ((MatchplayGuardianGame) game).getPlayerBattleStates().stream()
                        .filter(x -> x.getPosition() == playerPos)
                        .findFirst()
                        .orElse(null);

        if (playerBattleState == null)
            throw new ValidationException("playerBattleState is null");

        return (short) playerBattleState.getCurrentHealth().get();
    }
}

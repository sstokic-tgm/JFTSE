package com.jftse.emulator.server.core.matchplay.combat;

import com.jftse.emulator.common.exception.ValidationException;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.core.matchplay.battle.GuardianBattleState;
import com.jftse.emulator.server.core.matchplay.battle.PlayerBattleState;
import com.jftse.emulator.server.core.utils.BattleUtils;
import com.jftse.emulator.server.database.model.battle.WillDamage;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GuardianCombatSystem implements GuardianCombatable {
    private MatchplayGuardianGame game;

    public GuardianCombatSystem(MatchplayGuardianGame game) {
        this.game = game;
    }

    @Override
    public short dealDamage(int attackerPos, int targetPos, short damage, boolean hasAttackerDmgBuff, boolean hasTargetDefBuff) throws ValidationException {
        int totalDamageToDeal = damage;
        PlayerBattleState attackingPlayer = game.getPlayerBattleStates().stream()
                .filter(x -> x.getPosition() == attackerPos)
                .findFirst()
                .orElse(null);

        boolean isNormalDamageSkill = Math.abs(damage) != 1;
        if (attackingPlayer != null && isNormalDamageSkill) {
            totalDamageToDeal = BattleUtils.calculateDmg(attackingPlayer.getStr(), damage, hasAttackerDmgBuff);
        }

        GuardianBattleState targetGuardian = game.getGuardianBattleStates().stream()
                .filter(x -> x.getPosition() == targetPos)
                .findFirst()
                .orElse(null);

        if (targetGuardian == null)
            throw new ValidationException("targetGuardian battle state is null");

        if (isNormalDamageSkill) {
            int damageToDeny = BattleUtils.calculateDef(targetGuardian.getSta(), Math.abs(totalDamageToDeal), hasTargetDefBuff);
            if (damageToDeny > Math.abs(totalDamageToDeal)) {
                totalDamageToDeal = -1;
            } else {
                totalDamageToDeal += damageToDeny;
            }
        }

        return updateHealthByDamage(targetGuardian, totalDamageToDeal);
    }

    @Override
    public short dealDamageOnBallLoss(int attackerPos, int targetPos, boolean hasAttackerWillBuff) throws ValidationException {
        GuardianBattleState targetGuardian = game.getGuardianBattleStates().stream()
                .filter(x -> x.getPosition() == targetPos)
                .findFirst()
                .orElse(null);

        if (targetGuardian == null)
            throw new ValidationException("targetGuardian battle state is null");

        int lossBallDamage = 0;
        boolean servingGuardianScored = attackerPos == 4;
        if (servingGuardianScored) {
            lossBallDamage = (short) -(targetGuardian.getMaxHealth() * 0.02);
        } else {
            PlayerBattleState attackingPlayer = game.getPlayerBattleStates().stream()
                    .filter(x -> x.getPosition() == attackerPos)
                    .findFirst()
                    .orElse(null);
            if (attackingPlayer != null) {
                int playerWill = attackingPlayer.getWill();
                WillDamage willDamage = game.getWillDamages().stream()
                        .filter(x -> x.getWill() == playerWill)
                        .findFirst()
                        .orElse(game.getWillDamages().get(game.getWillDamages().size() - 1));
                lossBallDamage = -BattleUtils.calculateBallDamageByWill(willDamage, hasAttackerWillBuff);

                int additionalWillDmg = (int) (targetGuardian.getMaxHealth() * (playerWill / 10000d));
                lossBallDamage -= additionalWillDmg;
            }
        }

        return updateHealthByDamage(targetGuardian, lossBallDamage);
    }

    @Override
    public short heal(int targetPos, short percentage) throws ValidationException {
        GuardianBattleState targetGuardian = game.getGuardianBattleStates().stream()
                .filter(x -> x.getPosition() == targetPos)
                .findFirst()
                .orElse(null);

        if (targetGuardian == null)
            throw new ValidationException("targetGuardian battle state is null");

        percentage = game.getGuardianHealPercentage();

        int currentHealth = targetGuardian.getCurrentHealth().get();
        short healthToHeal = (short) (targetGuardian.getMaxHealth() * (percentage / 100f));
        currentHealth = Math.max(currentHealth, 0);
        short newGuardianHealth = (short) (currentHealth + healthToHeal);
        if (newGuardianHealth > targetGuardian.getMaxHealth()) {
            newGuardianHealth = (short) targetGuardian.getMaxHealth();
        }

        if (targetGuardian.getCurrentHealth().compareAndSet(currentHealth, newGuardianHealth))
            return newGuardianHealth;
        else
            return (short) currentHealth;
    }

    @Override
    public short dealDamageToPlayer(int attackerPos, int targetPos, short damage, boolean hasAttackerDmgBuff, boolean hasTargetDefBuff) throws ValidationException {
        int totalDamageToDeal = damage;
        GuardianBattleState attackingGuardian = game.getGuardianBattleStates().stream()
                .filter(x -> x.getPosition() == attackerPos)
                .findFirst()
                .orElse(null);

        boolean isNormalDamageSkill = Math.abs(damage) != 1;
        if (attackingGuardian != null && isNormalDamageSkill) {
            totalDamageToDeal = BattleUtils.calculateDmg(attackingGuardian.getStr(), damage, hasAttackerDmgBuff);
        }

        PlayerBattleState targetPlayer = game.getPlayerBattleStates().stream()
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
        }

        return updateHealthByDamage(targetPlayer, totalDamageToDeal);
    }

    @Override
    public short dealDamageOnBallLossToPlayer(int attackerPos, int targetPos, boolean hasAttackerWillBuff) throws ValidationException {
        PlayerBattleState targetPlayer = game.getPlayerBattleStates().stream()
                .filter(x -> x.getPosition() == targetPos)
                .findFirst()
                .orElse(null);

        if (targetPlayer == null)
            throw new ValidationException("targetPlayer battle state is null");

        int lossBallDamage = 0;
        boolean servingGuardianScored = attackerPos == 4;
        if (servingGuardianScored) {
            lossBallDamage = (short) -(targetPlayer.getMaxHealth() * 0.02);
        } else {
            GuardianBattleState attackingGuardian = game.getGuardianBattleStates().stream()
                    .filter(x -> x.getPosition() == attackerPos)
                    .findFirst()
                    .orElse(null);
            if (attackingGuardian != null) {
                int guardianWill = attackingGuardian.getWill();
                WillDamage willDamage = game.getWillDamages().stream()
                        .filter(x -> x.getWill() == guardianWill)
                        .findFirst()
                        .orElse(game.getWillDamages().get(game.getWillDamages().size() - 1));
                lossBallDamage = -BattleUtils.calculateBallDamageByWill(willDamage, hasAttackerWillBuff);
            }
        }

        return updateHealthByDamage(targetPlayer, lossBallDamage);
    }

    @Override
    public short updateHealthByDamage(GuardianBattleState targetGuardian, int dmg) {
        int currentHealth = targetGuardian.getCurrentHealth().get();
        currentHealth = Math.max(currentHealth, 0);
        log.debug("updateHealthByDamage(GuardianBattleState targetGuardian, int dmg): " + currentHealth + ", " + dmg);
        short newGuardianHealth = (short) (currentHealth + dmg);
        newGuardianHealth = newGuardianHealth < 0 ? 0 : newGuardianHealth;

        if (targetGuardian.getCurrentHealth().compareAndSet(currentHealth, newGuardianHealth))
            return newGuardianHealth;
        else
            return (short) currentHealth;
    }

    @Override
    public short updateHealthByDamage(PlayerBattleState targetPlayer, int dmg) {
        int currentHealth = targetPlayer.getCurrentHealth().get();
        currentHealth = Math.max(currentHealth, 0);
        log.debug("updateHealthByDamage(PlayerBattleState targetPlayer, int dmg): " + currentHealth + ", " + dmg);
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
    public GuardianBattleState reviveAnyGuardian(short revivePercentage) throws ValidationException {
        GuardianBattleState guardianBattleState = game.getGuardianBattleStates().stream()
                .filter(x -> x.getCurrentHealth().get() < 1)
                .findFirst()
                .orElse(null);

        if (guardianBattleState != null) {
            revivePercentage = game.getGuardianHealPercentage();
            heal(guardianBattleState.getPosition(), revivePercentage);
        }

        return guardianBattleState;
    }
}

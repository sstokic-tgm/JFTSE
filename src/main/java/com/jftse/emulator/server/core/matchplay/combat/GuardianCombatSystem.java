package com.jftse.emulator.server.core.matchplay.combat;

import com.jftse.emulator.common.exception.ValidationException;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.core.matchplay.battle.GuardianBattleState;
import com.jftse.emulator.server.core.matchplay.battle.PlayerBattleState;
import com.jftse.emulator.server.core.utils.BattleUtils;
import com.jftse.emulator.server.database.model.battle.WillDamage;

public class GuardianCombatSystem implements GuardianCombatable {
    private MatchplayGuardianGame game;

    public GuardianCombatSystem(MatchplayGuardianGame game) {
        this.game = game;
    }

    @Override
    public short dealDamage(int attackerPos, int targetPos, short damage, boolean hasAttackerDmgBuff, boolean hasTargetDefBuff) throws ValidationException {
        int totalDamageToDeal = damage;
        PlayerBattleState attackingPlayer = game.getPlayerBattleStates().stream()
                .filter(x -> x.getPosition().get() == attackerPos)
                .findFirst()
                .orElse(null);

        boolean isNormalDamageSkill = Math.abs(damage) != 1;
        if (attackingPlayer != null && isNormalDamageSkill) {
            totalDamageToDeal = BattleUtils.calculateDmg(attackingPlayer.getStr().get(), damage, hasAttackerDmgBuff);
        }

        GuardianBattleState targetGuardian = game.getGuardianBattleStates().stream()
                .filter(x -> x.getPosition().get() == targetPos)
                .findFirst()
                .orElse(null);

        if (targetGuardian == null)
            throw new ValidationException("targetGuardian battle state is null");

        if (isNormalDamageSkill) {
            int damageToDeny = BattleUtils.calculateDef(targetGuardian.getSta().get(), Math.abs(totalDamageToDeal), hasTargetDefBuff);
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
                .filter(x -> x.getPosition().get() == targetPos)
                .findFirst()
                .orElse(null);

        if (targetGuardian == null)
            throw new ValidationException("targetGuardian battle state is null");

        int lossBallDamage = 0;
        boolean servingGuardianScored = attackerPos == 4;
        if (servingGuardianScored) {
            lossBallDamage = (short) -(targetGuardian.getMaxHealth().get() * 0.02);
        } else {
            PlayerBattleState attackingPlayer = game.getPlayerBattleStates().stream()
                    .filter(x -> x.getPosition().get() == attackerPos)
                    .findFirst()
                    .orElse(null);
            if (attackingPlayer != null) {
                int playerWill = attackingPlayer.getWill().get();
                WillDamage willDamage = game.getWillDamages().stream()
                        .filter(x -> x.getWill() == playerWill)
                        .findFirst()
                        .orElse(game.getWillDamages().get(game.getWillDamages().size() - 1));
                lossBallDamage = -BattleUtils.calculateBallDamageByWill(willDamage, hasAttackerWillBuff);

                int additionalWillDmg = (int) (targetGuardian.getMaxHealth().get() * (playerWill / 10000d));
                lossBallDamage -= additionalWillDmg;
            }
        }

        return updateHealthByDamage(targetGuardian, lossBallDamage);
    }

    @Override
    public short heal(int targetPos, short percentage) throws ValidationException {
        GuardianBattleState targetGuardian = game.getGuardianBattleStates().stream()
                .filter(x -> x.getPosition().get() == targetPos)
                .findFirst()
                .orElse(null);

        if (targetGuardian == null)
            throw new ValidationException("targetGuardian battle state is null");

        percentage = game.getGuardianHealPercentage();

        short currentHealth = 0;
        short newGuardianHealth = 0;
        do {
            short healthToHeal = (short) (targetGuardian.getMaxHealth().get() * (percentage / 100f));
            currentHealth = (short) (Math.max(targetGuardian.getCurrentHealth().get(), 0));
            newGuardianHealth = (short) (currentHealth + healthToHeal);
            if (newGuardianHealth > targetGuardian.getMaxHealth().get()) {
                newGuardianHealth = (short) targetGuardian.getMaxHealth().get();
            }
        } while (!targetGuardian.getCurrentHealth().compareAndSet(currentHealth, newGuardianHealth));

        return newGuardianHealth;
    }

    @Override
    public short dealDamageToPlayer(int attackerPos, int targetPos, short damage, boolean hasAttackerDmgBuff, boolean hasTargetDefBuff) throws ValidationException {
        int totalDamageToDeal = damage;
        GuardianBattleState attackingGuardian = game.getGuardianBattleStates().stream()
                .filter(x -> x.getPosition().get() == attackerPos)
                .findFirst()
                .orElse(null);

        boolean isNormalDamageSkill = Math.abs(damage) != 1;
        if (attackingGuardian != null && isNormalDamageSkill) {
            totalDamageToDeal = BattleUtils.calculateDmg(attackingGuardian.getStr().get(), damage, hasAttackerDmgBuff);
        }

        PlayerBattleState targetPlayer = game.getPlayerBattleStates().stream()
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
    public short dealDamageOnBallLossToPlayer(int attackerPos, int targetPos, boolean hasAttackerWillBuff) throws ValidationException {
        PlayerBattleState targetPlayer = game.getPlayerBattleStates().stream()
                .filter(x -> x.getPosition().get() == targetPos)
                .findFirst()
                .orElse(null);

        if (targetPlayer == null)
            throw new ValidationException("targetPlayer battle state is null");

        int lossBallDamage = 0;
        boolean servingGuardianScored = attackerPos == 4;
        if (servingGuardianScored) {
            lossBallDamage = (short) -(targetPlayer.getMaxHealth().get() * 0.02);
        } else {
            GuardianBattleState attackingGuardian = game.getGuardianBattleStates().stream()
                    .filter(x -> x.getPosition().get() == attackerPos)
                    .findFirst()
                    .orElse(null);
            if (attackingGuardian != null) {
                int guardianWill = attackingGuardian.getWill().get();
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
        short currentHealth = 0;
        short newGuardianHealth = 0;
        do {
            currentHealth = (short) Math.max(targetGuardian.getCurrentHealth().get(), 0);
            newGuardianHealth = (short) (currentHealth + dmg);
            newGuardianHealth = newGuardianHealth < 0 ? 0 : newGuardianHealth;
        } while (!targetGuardian.getCurrentHealth().compareAndSet(currentHealth, newGuardianHealth));

        return newGuardianHealth;
    }

    @Override
    public short updateHealthByDamage(PlayerBattleState targetPlayer, int dmg) {
        short newPlayerHealth = 0;
        short currentHealth = 0;
        do {
            currentHealth = (short) Math.max(targetPlayer.getCurrentHealth().get(), 0);
            newPlayerHealth = (short) (currentHealth + dmg);
            if (newPlayerHealth < 1) {
                targetPlayer.getDead().getAndSet(true);
            }
            newPlayerHealth = newPlayerHealth < 0 ? 0 : newPlayerHealth;
        } while (!targetPlayer.getCurrentHealth().compareAndSet(currentHealth, newPlayerHealth));

        return newPlayerHealth;
    }

    @Override
    public GuardianBattleState reviveAnyGuardian(short revivePercentage) throws ValidationException {
        GuardianBattleState guardianBattleState = game.getGuardianBattleStates().stream()
                .filter(x -> x.getCurrentHealth().get() < 1)
                .findFirst()
                .orElse(null);

        if (guardianBattleState != null) {
            revivePercentage = game.getGuardianHealPercentage();
            heal(guardianBattleState.getPosition().get(), revivePercentage);
        }

        return guardianBattleState;
    }
}

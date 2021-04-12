package com.jftse.emulator.server.game.core.matchplay.basic;

import com.jftse.emulator.common.exception.ValidationException;
import com.jftse.emulator.server.database.model.battle.GuardianStage;
import com.jftse.emulator.server.database.model.battle.WillDamage;
import com.jftse.emulator.server.game.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.game.core.matchplay.PlayerReward;
import com.jftse.emulator.server.game.core.matchplay.battle.GuardianBattleState;
import com.jftse.emulator.server.game.core.matchplay.battle.PlayerBattleState;
import com.jftse.emulator.server.game.core.matchplay.battle.SkillCrystal;
import com.jftse.emulator.server.game.core.utils.BattleUtils;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class MatchplayBattleGame extends MatchplayGame {
    private long crystalSpawnInterval;
    private long crystalDeSpawnInterval;
    private List<Point> playerLocationsOnMap;
    private List<PlayerBattleState> playerBattleStates;
    private List<SkillCrystal> skillCrystals;
    private List<WillDamage> willDamages;
    private short lastCrystalId = -1;
    private short lastGuardianServeSide;
    private Date stageStartTime;

    public MatchplayBattleGame() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        this.setStartTime(cal.getTime());
        this.setStageStartTime(cal.getTime());
        this.playerBattleStates = new ArrayList<>();
        this.skillCrystals = new ArrayList<>();
        this.playerLocationsOnMap = Arrays.asList(
                new Point(20, -75),
                new Point(-20, -75));
        this.willDamages = new ArrayList<>();
        this.setFinished(false);
    }

    public short damagePlayer(int guardianPos, int playerPos, short damage, boolean hasAttackerDmgBuff, boolean hasReceiverDefBuff) throws ValidationException {
        int totalDamageToDeal = damage;
        PlayerBattleState attackingPlayer = this.playerBattleStates.stream()
                .filter(x -> x.getPosition() == guardianPos)
                .findFirst()
                .orElse(null);

        boolean isNormalDamageSkill = Math.abs(damage) != 1;
        if (attackingPlayer != null && isNormalDamageSkill) {
            totalDamageToDeal = BattleUtils.calculateDmg(attackingPlayer.getStr(), damage, hasAttackerDmgBuff);
        }

        PlayerBattleState playerBattleState = this.playerBattleStates.stream()
                .filter(x -> x.getPosition() == playerPos)
                .findFirst()
                .orElse(null);

        if (playerBattleState == null)
            throw new ValidationException("playerBattleState is null");

        if (isNormalDamageSkill) {
            int damageToDeny = BattleUtils.calculateDef(playerBattleState.getSta(), Math.abs(totalDamageToDeal), hasReceiverDefBuff);
            if (damageToDeny > Math.abs(totalDamageToDeal)) {
                totalDamageToDeal = -1;
            } else {
                totalDamageToDeal += damageToDeny;
            }
        }

        short newPlayerHealth = (short) (playerBattleState.getCurrentHealth() + totalDamageToDeal);
        if (newPlayerHealth < 1) {
            playerBattleState.setDead(true);
        }

        newPlayerHealth = newPlayerHealth < 0 ? 0 : newPlayerHealth;
        playerBattleState.setCurrentHealth(newPlayerHealth);
        return newPlayerHealth;
    }

    public short damagePlayerOnBallLoss(int playerPos, int attackerPos, boolean hasAttackerWillBuff) throws ValidationException {
        PlayerBattleState playerBattleState = this.playerBattleStates.stream()
                .filter(x -> x.getPosition() == playerPos)
                .findFirst()
                .orElse(null);

        if (playerBattleState == null)
            throw new ValidationException("playerBattleState is null");

        int lossBallDamage = 0;
        boolean servingGuardianScored = attackerPos == 4;
        if (servingGuardianScored) {
            lossBallDamage = (short) -(playerBattleState.getMaxHealth() * 0.1);
        } else {
            PlayerBattleState attackingPlayerBattleState = this.playerBattleStates.stream()
                    .filter(x -> x.getPosition() == attackerPos)
                    .findFirst()
                    .orElse(null);
            if (attackingPlayerBattleState != null) {
                WillDamage willDamage = this.getWillDamages().stream()
                        .filter(x -> x.getWill() == attackingPlayerBattleState.getWill())
                        .findFirst()
                        .orElse(this.getWillDamages().get(0));
                lossBallDamage = -BattleUtils.calculateBallDamageByWill(willDamage, hasAttackerWillBuff);
            }
        }

        short newPlayerHealth = (short) (playerBattleState.getCurrentHealth() + lossBallDamage);
        if (newPlayerHealth < 1) {
            playerBattleState.setDead(true);
        }

        newPlayerHealth = newPlayerHealth < 0 ? 0 : newPlayerHealth;
        playerBattleState.setCurrentHealth(newPlayerHealth);
        return newPlayerHealth;
    }

    public short healPlayer(int playerPos, short percentage) throws ValidationException {
        PlayerBattleState playerBattleState = this.playerBattleStates.stream()
                .filter(x -> x.getPosition() == playerPos)
                .findFirst()
                .orElse(null);

        if (playerBattleState == null)
            throw new ValidationException("playerBattleState is null");

        short healthToHeal = (short) (playerBattleState.getMaxHealth() * (percentage / 100f));
        short currentHealth = (short) (playerBattleState.getCurrentHealth() < 0 ? 0 : playerBattleState.getCurrentHealth());
        short newPlayerHealth = (short) (currentHealth + healthToHeal);
        if (newPlayerHealth > playerBattleState.getMaxHealth()) {
            newPlayerHealth = playerBattleState.getMaxHealth();
        }

        playerBattleState.setCurrentHealth(newPlayerHealth);
        return newPlayerHealth;
    }

    public PlayerBattleState reviveAnyPlayer(short revivePercentage) throws ValidationException {
        // TODO: ONLY OWN TEAM
        PlayerBattleState playerBattleState = getPlayerBattleStates().stream()
                .filter(x -> x.isDead())
                .findFirst()
                .orElse(null);
        if (playerBattleState != null) {
            short newPlayerHealth = healPlayer(playerBattleState.getPosition(), revivePercentage);
            playerBattleState.setCurrentHealth(newPlayerHealth);
            playerBattleState.setDead(false);
        }

        return playerBattleState;
    }

    public short getPlayerCurrentHealth(short playerPos) throws ValidationException {
        PlayerBattleState playerBattleState = this.playerBattleStates.stream()
                .filter(x -> x.getPosition() == playerPos)
                .findFirst()
                .orElse(null);

        if (playerBattleState == null)
            throw new ValidationException("playerBattleState is null");

        return playerBattleState.getCurrentHealth();
    }

    public long getStageTimePlayingInSeconds() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        long duration = cal.getTime().getTime() - this.getStageStartTime().getTime();
        return TimeUnit.MILLISECONDS.toSeconds(duration);
    }

    public List<PlayerReward> getPlayerRewards() {
        return new ArrayList<>();
    }

    @Override
    public long getTimeNeeded() {
        return getEndTime().getTime() - getStartTime().getTime();
    }

    @Override
    public boolean isRedTeam(int playerPos) {
        return playerPos == 0 || playerPos == 2;
    }

    @Override
    public boolean isBlueTeam(int playerPos) {
        return playerPos == 1 || playerPos == 3;
    }
}

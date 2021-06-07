package com.jftse.emulator.server.game.core.matchplay.basic;

import com.jftse.emulator.common.exception.ValidationException;
import com.jftse.emulator.server.database.model.battle.WillDamage;
import com.jftse.emulator.server.game.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.game.core.matchplay.PlayerReward;
import com.jftse.emulator.server.game.core.matchplay.battle.PlayerBattleState;
import com.jftse.emulator.server.game.core.matchplay.battle.SkillCrystal;
import com.jftse.emulator.server.game.core.matchplay.room.RoomPlayer;
import com.jftse.emulator.server.game.core.utils.BattleUtils;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class MatchplayBattleGame extends MatchplayGame {
    private long crystalSpawnInterval;
    private long crystalDeSpawnInterval;
    private List<Point> playerLocationsOnMap;
    private ConcurrentLinkedDeque<PlayerBattleState> playerBattleStates;
    private List<SkillCrystal> skillCrystals;
    private List<WillDamage> willDamages;
    private short lastCrystalId = -1;
    private short lastGuardianServeSide;
    private Date stageStartTime;

    public MatchplayBattleGame() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        this.setStartTime(cal.getTime());
        this.setStageStartTime(cal.getTime());
        this.playerBattleStates = new ConcurrentLinkedDeque<>();
        this.skillCrystals = new ArrayList<>();
        this.playerLocationsOnMap = Arrays.asList(
                new Point(20, -125),
                new Point(-20, 125),
                new Point(-20, -75),
                new Point(20, 75)
        );
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

    public PlayerBattleState reviveAnyPlayer(short revivePercentage, Optional<RoomPlayer> roomPlayer) throws ValidationException {
        if (roomPlayer.isPresent()) {
            RoomPlayer rp = roomPlayer.get();
            boolean isRedTeam = this.isRedTeam(rp.getPosition());

            PlayerBattleState playerBattleState = getPlayerBattleStates().stream()
                    .filter(x -> isRedTeam == this.isRedTeam(x.getPosition()) && x.isDead() || !isRedTeam == !this.isRedTeam(x.getPosition()) && x.isDead())
                    .findFirst()
                    .orElse(null);

            if (playerBattleState != null) {
                short newPlayerHealth = healPlayer(playerBattleState.getPosition(), revivePercentage);
                playerBattleState.setCurrentHealth(newPlayerHealth);
                playerBattleState.setDead(false);
            }
            return playerBattleState;
        }
        return null;
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

    public List<Integer> getPlayerPositionsOrderedByHighestHealth() {
        List<Integer> playerPositions = new ArrayList<>();
        this.playerBattleStates.stream()
                .sorted(Comparator.comparingInt(PlayerBattleState::getCurrentHealth)
                        .reversed())
                .forEach(p -> playerPositions.add((int) p.getPosition()));

        return playerPositions;
    }

    public List<PlayerReward> getPlayerRewards() {
        int secondsPlayed = (int) Math.ceil((double) this.getTimeNeeded() / 1000);
        List<PlayerReward> playerRewards = new ArrayList<>();

        int iteration = 0;
        for (int playerPosition : this.getPlayerPositionsOrderedByHighestHealth()) {
            boolean wonGame = false;
            boolean isPlayerInRedTeam = this.isRedTeam(playerPosition);
            boolean allPlayersTeamRedDead = this.getPlayerBattleStates().stream().filter(x -> this.isRedTeam(x.getPosition())).allMatch(x -> x.getCurrentHealth() < 1);
            boolean allPlayersTeamBlueDead = this.getPlayerBattleStates().stream().filter(x -> this.isBlueTeam(x.getPosition())).allMatch(x -> x.getCurrentHealth() < 1);
            if (isPlayerInRedTeam && allPlayersTeamBlueDead || !isPlayerInRedTeam && allPlayersTeamRedDead) {
                wonGame = true;
            }

            int basicExpReward;
            if (secondsPlayed > TimeUnit.MINUTES.toSeconds(15)) {
                basicExpReward = 130;
            } else {
                basicExpReward = (int) Math.round(30 + (secondsPlayed - 90) * 0.12);
            }

            switch (iteration) {
                case 0:
                    basicExpReward += basicExpReward * 0.1;
                    break;
                case 1:
                    basicExpReward += basicExpReward * 0.05;
                    break;
            }

            if (wonGame) {
                basicExpReward += basicExpReward * 0.2;
            }

            int rewardExp = basicExpReward;
            int rewardGold = basicExpReward;
            PlayerReward playerReward = new PlayerReward();
            playerReward.setPlayerPosition(playerPosition);
            playerReward.setRewardExp(rewardExp);
            playerReward.setRewardGold(rewardGold);
            playerRewards.add(playerReward);

            iteration++;
        }

        return playerRewards;
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

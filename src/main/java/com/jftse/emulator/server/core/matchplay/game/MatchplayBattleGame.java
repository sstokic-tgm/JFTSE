package com.jftse.emulator.server.core.matchplay.game;

import com.jftse.emulator.server.core.constants.GameFieldSide;
import com.jftse.emulator.server.core.matchplay.combat.PlayerCombatSystem;
import com.jftse.emulator.server.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.core.matchplay.PlayerReward;
import com.jftse.emulator.server.core.matchplay.battle.PlayerBattleState;
import com.jftse.emulator.server.core.matchplay.battle.SkillCrystal;

import com.jftse.emulator.server.core.matchplay.room.RoomPlayer;
import com.jftse.emulator.server.core.utils.BattleUtils;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Getter
@Setter
public class MatchplayBattleGame extends MatchplayGame {
    private AtomicLong crystalSpawnInterval;
    private AtomicLong crystalDeSpawnInterval;
    private ConcurrentLinkedDeque<Point> playerLocationsOnMap;
    private ConcurrentLinkedDeque<PlayerBattleState> playerBattleStates;
    private ConcurrentLinkedDeque<SkillCrystal> skillCrystals;
    private AtomicInteger lastCrystalId;
    private AtomicInteger lastGuardianServeSide;
    private AtomicInteger spiderMineIdentifier;
    private ConcurrentLinkedDeque<ScheduledFuture<?>> scheduledFutures;

    private final PlayerCombatSystem playerCombatSystem;

    public MatchplayBattleGame() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        this.setStartTime(cal.getTime());
        this.crystalSpawnInterval = new AtomicLong(0);
        this.crystalDeSpawnInterval = new AtomicLong(0);
        this.playerLocationsOnMap = new ConcurrentLinkedDeque<>(Arrays.asList(
                new Point(20, -125),
                new Point(-20, 125),
                new Point(-20, -75),
                new Point(20, 75)
        ));
        this.playerBattleStates = new ConcurrentLinkedDeque<>();
        this.skillCrystals = new ConcurrentLinkedDeque<>();
        this.lastCrystalId = new AtomicInteger(-1);
        this.lastGuardianServeSide = new AtomicInteger(GameFieldSide.RedTeam);
        this.willDamages = new ArrayList<>();
        this.spiderMineIdentifier = new AtomicInteger(0);
        this.scheduledFutures = new ConcurrentLinkedDeque<>();
        this.setFinished(new AtomicBoolean(false));

        playerCombatSystem = new PlayerCombatSystem(this);
    }

    public PlayerBattleState createPlayerBattleState(RoomPlayer roomPlayer) {
        short baseHp = (short) BattleUtils.calculatePlayerHp(roomPlayer.getPlayer());
        short baseStr = roomPlayer.getPlayer().getStrength();
        short baseSta = roomPlayer.getPlayer().getStamina();
        short baseDex = roomPlayer.getPlayer().getDexterity();
        short baseWill = roomPlayer.getPlayer().getWillpower();
        short totalHp = (short) (baseHp + roomPlayer.getStatusPointsAddedDto().getAddHp());
        short totalStr = (short) (baseStr + roomPlayer.getStatusPointsAddedDto().getStrength());
        short totalSta = (short) (baseSta + roomPlayer.getStatusPointsAddedDto().getStamina());
        short totalDex = (short) (baseDex + roomPlayer.getStatusPointsAddedDto().getDexterity());
        short totalWill = (short) (baseWill + roomPlayer.getStatusPointsAddedDto().getWillpower());
        return new PlayerBattleState(roomPlayer.getPosition(), totalHp, totalStr, totalSta, totalDex, totalWill);
    }

    public List<Integer> getPlayerPositionsOrderedByHighestHealth() {
        List<Integer> playerPositions = new ArrayList<>();
        this.playerBattleStates.stream()
                .sorted(Comparator.comparingInt(pbs -> ((PlayerBattleState) pbs).getCurrentHealth().get())
                        .reversed())
                .forEach(p -> playerPositions.add(p.getPosition().get()));

        return playerPositions;
    }

    public List<PlayerReward> getPlayerRewards() {
        int secondsPlayed = (int) Math.ceil((double) this.getTimeNeeded() / 1000);
        List<PlayerReward> playerRewards = new ArrayList<>();

        int iteration = 0;
        for (int playerPosition : this.getPlayerPositionsOrderedByHighestHealth()) {
            boolean wonGame = false;
            boolean isPlayerInRedTeam = this.isRedTeam(playerPosition);
            boolean allPlayersTeamRedDead = this.getPlayerBattleStates().stream()
                    .filter(x -> this.isRedTeam(x.getPosition().get()))
                    .allMatch(x -> x.getCurrentHealth().get() < 1);
            boolean allPlayersTeamBlueDead = this.getPlayerBattleStates().stream()
                    .filter(x -> this.isBlueTeam(x.getPosition().get()))
                    .allMatch(x -> x.getCurrentHealth().get() < 1);
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
            if (wonGame) { // TODO: temporarily only
                playerReward.setRewardProductIndex(57592);
                playerReward.setProductRewardAmount(1);
            }

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

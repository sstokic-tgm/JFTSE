package com.jftse.emulator.server.core.matchplay.game;

import com.jftse.emulator.server.core.constants.ServeType;
import com.jftse.emulator.server.core.life.progression.ExpGoldBonus;
import com.jftse.emulator.server.core.life.progression.ExpGoldBonusImpl;
import com.jftse.emulator.server.core.life.progression.SimpleExpGoldBonus;
import com.jftse.emulator.server.core.life.progression.bonuses.WonGameBonus;
import com.jftse.emulator.server.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.core.matchplay.PlayerReward;
import com.jftse.emulator.server.core.matchplay.room.RoomPlayer;
import com.jftse.emulator.server.core.matchplay.room.ServeInfo;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class MatchplayBasicGame extends MatchplayGame {
    private HashMap<Integer, Integer> individualPointsMadeFromPlayers;
    private ArrayList<Point> playerLocationsOnMap;
    private int pointsRedTeam;
    private int pointsBlueTeam;
    private int setsRedTeam;
    private int setsBlueTeam;
    private RoomPlayer servePlayer;
    private RoomPlayer receiverPlayer;

    public MatchplayBasicGame(byte players) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        this.setStartTime(cal.getTime());
        this.playerLocationsOnMap = new ArrayList<>(Arrays.asList(
                new Point(20, -125),
                new Point(-20, 125),
                new Point(-20, -75),
                new Point(20, 75)
        ));

        this.individualPointsMadeFromPlayers = new HashMap<>(players);
        for (int i = 0; i < players; i++) {
            this.individualPointsMadeFromPlayers.put(i, 0);
        }
    }

    public void setPoints(byte pointsRedTeam, byte pointsBlueTeam) {
        this.pointsRedTeam = pointsRedTeam;
        this.pointsBlueTeam = pointsBlueTeam;

        if (pointsRedTeam == 4 && pointsBlueTeam < 3) {
            this.setsRedTeam += 1;
            resetPoints();
        }
        else if (pointsRedTeam > 4 && (pointsRedTeam - pointsBlueTeam) == 2) {
            this.setsRedTeam += 1;
            resetPoints();
        }
        else if (pointsBlueTeam == 4 && pointsRedTeam < 3) {
            this.setsBlueTeam += 1;
            resetPoints();
        }
        else if (pointsBlueTeam > 4 && (pointsBlueTeam - pointsRedTeam) == 2) {
            this.setsBlueTeam += 1;
            resetPoints();
        }

        if (this.setsRedTeam == 2 || this.setsBlueTeam == 2) {
            this.setFinished(true);

            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            this.setEndTime(cal.getTime());
        }
    }

    public void increasePerformancePointForPlayer(int playerPosition) {
        int currentPoint = this.getIndividualPointsMadeFromPlayers().getOrDefault(playerPosition, 0);
        this.getIndividualPointsMadeFromPlayers().put(playerPosition, currentPoint + 1);
    }

    public List<Integer> getPlayerPositionsOrderedByPerformance() {
        List<Integer> playerPositions = new ArrayList<>();
        this.getIndividualPointsMadeFromPlayers().entrySet().stream()
                .sorted((k1, k2) -> -k1.getValue().compareTo(k2.getValue()))
                .forEach(k -> playerPositions.add(k.getKey()));
        return playerPositions;
    }

    public List<PlayerReward> getPlayerRewards() {
        int secondsPlayed = (int) Math.ceil((double) this.getTimeNeeded() / 1000);
        List<PlayerReward> playerRewards = new ArrayList<>();
        for (int playerPosition : this.getPlayerPositionsOrderedByPerformance()) {
            boolean wonGame = false;
            boolean isPlayerInRedTeam = this.isRedTeam(playerPosition);
            if (isPlayerInRedTeam && this.getSetsRedTeam() == 2 || !isPlayerInRedTeam && this.getSetsBlueTeam() == 2) {
                wonGame = true;
            }

            int basicExpReward;
            if (secondsPlayed < TimeUnit.MINUTES.toSeconds(2)) {
                basicExpReward = 1;
            } else if (secondsPlayed > TimeUnit.MINUTES.toSeconds(15)) {
                basicExpReward = 130;
            } else {
                basicExpReward = (int) Math.round(30 + (secondsPlayed - 90) * 0.12);
            }

            ExpGoldBonus expGoldBonus = new ExpGoldBonusImpl(basicExpReward, basicExpReward);

            int playerPositionIndex = this.getPlayerPositionsOrderedByPerformance().indexOf(playerPosition);
            switch (playerPositionIndex) {
                case 0 -> expGoldBonus = new SimpleExpGoldBonus(expGoldBonus, 0.1);
                case 1 -> expGoldBonus = new SimpleExpGoldBonus(expGoldBonus, 0.05);
            }

            if (wonGame) {
                expGoldBonus = new WonGameBonus(expGoldBonus);
            }

            int rewardExp = expGoldBonus.calculateExp();
            int rewardGold = expGoldBonus.calculateGold();
            PlayerReward playerReward = new PlayerReward();
            playerReward.setPlayerPosition(playerPosition);
            playerReward.setRewardExp(rewardExp);
            playerReward.setRewardGold(rewardGold);
            if (wonGame) { // TODO: temporarily only
                playerReward.setRewardProductIndex(57592);
                playerReward.setProductRewardAmount(1);
            }

            playerRewards.add(playerReward);
        }

        return playerRewards;
    }

    private void resetPoints() {
        this.pointsRedTeam = 0;
        this.pointsBlueTeam = 0;
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

    public Point invertPointX(Point point) {
        return new Point(point.x * (-1), point.y);
    }

    public Point invertPointY(Point point) {
        return new Point(point.x, point.y  * (-1));
    }

    public boolean shouldSwitchServingSide(boolean isSingles, boolean isRedTeamServing, boolean anyTeamWonSet, int playerPosition) {
        if (anyTeamWonSet) {
            return false;
        }

        if (isSingles) {
            return true;
        }
        return isRedTeamServing && this.isRedTeam(playerPosition) || !isRedTeamServing && this.isBlueTeam(playerPosition);
    }

    public boolean isRedTeamServing(int timesCourtChanged) {
        return this.isEven(timesCourtChanged);
    }

    public boolean shouldPlayerServe(boolean isSingles, int timesCourtChanged, int playerPosition) {
        if (isSingles) {
            if (this.isEven(playerPosition) && this.isEven(timesCourtChanged)) {
                return true;
            }

            return !this.isEven(playerPosition) && !this.isEven(timesCourtChanged);
        } else {
            return playerPosition == timesCourtChanged;
        }
    }

    public void setPlayerLocationsForDoubles(List<ServeInfo> serveInfo) {
        long outerFieldPosY = 125;
        long innerFieldPosY = 75;

        ServeInfo server = serveInfo.stream().filter(x -> x.getServeType() == ServeType.ServeBall).findFirst().orElse(null);
        if (server == null) return;

        Point serverLocation = server.getPlayerStartLocation();
        serverLocation.setLocation(serverLocation.x, this.getCorrectCourtPositionY(serverLocation.y, outerFieldPosY));

        ServeInfo nonServer = serveInfo.stream()
                .filter(x -> this.areInSameTeam(server.getPlayerPosition(), x.getPlayerPosition()) && x.getPlayerStartLocation().x == serverLocation.x * (-1))
                .findFirst()
                .orElse(null);
        if (nonServer != null) {
            Point nonServerStartLocation = nonServer.getPlayerStartLocation();
            nonServer.getPlayerStartLocation().setLocation(nonServerStartLocation.x, this.getCorrectCourtPositionY(nonServerStartLocation.y, innerFieldPosY));
        }

        ServeInfo receiver = serveInfo.stream()
                .filter(x -> !this.areInSameTeam(server.getPlayerPosition(), x.getPlayerPosition()) && x.getPlayerStartLocation().x == serverLocation.x * (-1))
                .findFirst()
                .orElse(null);
        if (receiver != null) {
            Point receiverStartLocation = receiver.getPlayerStartLocation();
            receiver.getPlayerStartLocation().setLocation(receiverStartLocation.x, this.getCorrectCourtPositionY(receiverStartLocation.y, outerFieldPosY));
            receiver.setServeType(ServeType.ReceiveBall);
        }

        ServeInfo nonReceiver = serveInfo.stream()
                .filter(x -> !this.areInSameTeam(server.getPlayerPosition(), x.getPlayerPosition()) && x.getPlayerStartLocation().x == serverLocation.x)
                .findFirst()
                .orElse(null);
        if (nonReceiver != null) {
            Point nonReceiverPlayerStartLocation = nonReceiver.getPlayerStartLocation();
            nonReceiver.getPlayerStartLocation().setLocation(nonReceiverPlayerStartLocation.x, this.getCorrectCourtPositionY(nonReceiverPlayerStartLocation.y, innerFieldPosY));
        }
    }

    private boolean areInSameTeam(int playerPosition, int playerPosition1) {
        return this.isRedTeam(playerPosition) && this.isRedTeam(playerPosition1) ||
                this.isBlueTeam(playerPosition) && this.isBlueTeam(playerPosition1);
    }

    private long getCorrectCourtPositionY(long playerPositionY, long targetYPosition) {
        return playerPositionY > 0 ? targetYPosition : -targetYPosition;
    }

    private boolean isEven(int number) {
        return number % 2 == 0;
    }
}
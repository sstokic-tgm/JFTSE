package com.jftse.emulator.server.core.matchplay.game;

import com.jftse.emulator.server.core.constants.BonusIconHighlightValues;
import com.jftse.emulator.server.core.constants.ServeType;
import com.jftse.emulator.server.core.life.progression.ExpGoldBonus;
import com.jftse.emulator.server.core.life.progression.ExpGoldBonusImpl;
import com.jftse.emulator.server.core.life.progression.SimpleExpGoldBonus;
import com.jftse.emulator.server.core.life.progression.bonuses.*;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.life.room.ServeInfo;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.core.matchplay.MatchplayHandleable;
import com.jftse.emulator.server.core.matchplay.PlayerReward;

import com.jftse.emulator.server.core.matchplay.handler.MatchplayBasicModeHandler;
import com.jftse.entities.database.model.messenger.Friend;
import com.jftse.server.core.item.EItemCategory;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Getter
@Setter
public class MatchplayBasicGame extends MatchplayGame {
    private Map<Integer, Integer> individualPointsMadeFromPlayers;
    private List<Point> playerLocationsOnMap;
    private Map<Integer, Boolean> pointBackVotes;
    private AtomicInteger pointsRedTeam;
    private AtomicInteger pointsBlueTeam;
    private AtomicInteger setsRedTeam;
    private AtomicInteger setsBlueTeam;
    private AtomicReference<RoomPlayer> servePlayer;
    private AtomicReference<RoomPlayer> receiverPlayer;

    private Map<Integer, Integer> previousIndividualPointsMadeFromPlayers;
    private AtomicInteger previousPointsRedTeam;
    private AtomicInteger previousPointsBlueTeam;
    private AtomicInteger previousSetsRedTeam;
    private AtomicInteger previousSetsBlueTeam;
    private AtomicInteger previousServePlayerPosition;
    private AtomicInteger previousReceiverPlayerPosition;
    private AtomicBoolean setDowngraded = new AtomicBoolean(false);
    private AtomicBoolean pointBackValid = new AtomicBoolean(false);

    public MatchplayBasicGame(byte players) {
        super();

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        this.startTime = new AtomicReference<>(cal.getTime());

        this.playerLocationsOnMap = Arrays.asList(
                new Point(20, -125),
                new Point(-20, 125),
                new Point(-20, -75),
                new Point(20, 75));

        this.pointsRedTeam = new AtomicInteger(0);
        this.pointsBlueTeam = new AtomicInteger(0);
        this.setsRedTeam = new AtomicInteger(0);
        this.setsBlueTeam = new AtomicInteger(0);
        this.finished = new AtomicBoolean(false);
        this.pointBackVotes = new ConcurrentHashMap<>(players);
        this.previousIndividualPointsMadeFromPlayers = new ConcurrentHashMap<>(players);
        this.previousPointsRedTeam = new AtomicInteger(0);
        this.previousPointsBlueTeam = new AtomicInteger(0);
        this.previousSetsRedTeam = new AtomicInteger(0);
        this.previousSetsBlueTeam = new AtomicInteger(0);
        this.previousServePlayerPosition = new AtomicInteger(0);
        this.previousReceiverPlayerPosition = new AtomicInteger(0);
        this.servePlayer = new AtomicReference<>();
        this.receiverPlayer = new AtomicReference<>();
        this.scheduledFutures = new ConcurrentLinkedDeque<>();
        this.individualPointsMadeFromPlayers = new ConcurrentHashMap<>(players);
        for (int i = 0; i < players; i++) {
            this.individualPointsMadeFromPlayers.put(i, 0);
        }
    }

    public void setPoints(byte pointsRedTeam, byte pointsBlueTeam) {
        if (this.finished.get())
            return;

        this.previousPointsRedTeam.set(this.pointsRedTeam.get());
        this.previousPointsBlueTeam.set(this.pointsBlueTeam.get());
        this.previousSetsRedTeam.set(this.setsRedTeam.get());
        this.previousSetsBlueTeam.set(this.setsBlueTeam.get());
        this.previousIndividualPointsMadeFromPlayers.putAll(this.individualPointsMadeFromPlayers);
        if (this.servePlayer.get() != null)
            this.previousServePlayerPosition.set(this.servePlayer.get().getPosition());
        if (this.receiverPlayer.get() != null)
            this.previousReceiverPlayerPosition.set(this.receiverPlayer.get().getPosition());
        this.pointBackValid.set(true);

        this.pointsRedTeam.set(pointsRedTeam);
        this.pointsBlueTeam.set(pointsBlueTeam);

        if (pointsRedTeam == 4 && pointsBlueTeam < 3) {
            this.setsRedTeam.getAndIncrement();
            resetPoints();
        } else if (pointsRedTeam > 4 && (pointsRedTeam - pointsBlueTeam) == 2) {
            this.setsRedTeam.getAndIncrement();
            resetPoints();
        } else if (pointsBlueTeam == 4 && pointsRedTeam < 3) {
            this.setsBlueTeam.getAndIncrement();
            resetPoints();
        } else if (pointsBlueTeam > 4 && (pointsBlueTeam - pointsRedTeam) == 2) {
            this.setsBlueTeam.getAndIncrement();
            resetPoints();
        }

        if (this.setsRedTeam.get() == 2 || this.setsBlueTeam.get() == 2) {
            this.finished.set(true);

            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            this.endTime = new AtomicReference<>(cal.getTime());
        }
        resetPointBackVotes();
    }

    public synchronized void setPointBackVote(int playerPosition) {
        this.pointBackVotes.put(playerPosition, true);
    }

    public boolean isPointBackAvailable() {
        for (Map.Entry<Integer, Boolean> entry : this.pointBackVotes.entrySet()) {
            if (!entry.getValue())
                return false;
        }
        return this.pointBackValid.get();
    }

    public synchronized void pointBack() {
        this.pointsRedTeam.set(this.previousPointsRedTeam.get());
        this.pointsBlueTeam.set(this.previousPointsBlueTeam.get());
        //for example, if downgraded to 40 - 0 from 1 - 0 sets
        this.setDowngraded.set(this.previousSetsRedTeam.get() != this.setsRedTeam.get() || this.previousSetsBlueTeam.get() != this.setsBlueTeam.get());
        this.setsRedTeam.set(this.previousSetsRedTeam.get());
        this.setsBlueTeam.set(this.previousSetsBlueTeam.get());
        this.individualPointsMadeFromPlayers.putAll(this.previousIndividualPointsMadeFromPlayers);

        resetPointBackVotes();
        this.pointBackValid.set(false);
    }

    private void resetPointBackVotes() {
        for (Map.Entry<Integer, Boolean> entry : this.pointBackVotes.entrySet()) {
            entry.setValue(false);
        }
    }

    public synchronized void increasePerformancePointForPlayer(int playerPosition) {
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

    @Override
    public List<PlayerReward> getPlayerRewards() {
        int secondsPlayed = (int) Math.ceil((double) this.getTimeNeeded() / 1000);
        List<PlayerReward> playerRewards = new ArrayList<>();
        for (int playerPosition : this.getPlayerPositionsOrderedByPerformance()) {
            boolean wonGame = false;
            boolean isPlayerInRedTeam = this.isRedTeam(playerPosition);
            if (isPlayerInRedTeam && this.getSetsRedTeam().get() == 2 || !isPlayerInRedTeam && this.getSetsBlueTeam().get() == 2) {
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
            PlayerReward playerReward = new PlayerReward(playerPosition);
            playerReward.setExp(rewardExp);
            playerReward.setGold(rewardGold);

            List<Integer> materialsForReward = ServiceManager.getInstance().getItemMaterialService().findAllItemIndexes();
            List<Integer> materialsProductIndex = ServiceManager.getInstance().getProductService().findAllProductIndexesByCategoryAndItemIndexList(EItemCategory.MATERIAL.getName(), materialsForReward);
            materialsProductIndex.add(57592);

            Random rnd = new Random();
            int drawnMaterial = rnd.nextInt(materialsProductIndex.size() - 1 + 1) + 1;

            playerReward.setProductIndex(materialsProductIndex.get(drawnMaterial - 1));

            final int min = 1;
            final int max = !wonGame ? 2 : 3;
            final int amount = rnd.nextInt(max - min + 1) + min;
            playerReward.setProductAmount(amount);

            playerRewards.add(playerReward);
        }

        return playerRewards;
    }

    @Override
    public void addBonusesToRewards(ConcurrentLinkedDeque<RoomPlayer> roomPlayers, List<PlayerReward> playerRewards) {
        final boolean isSingles = roomPlayers.stream().filter(rp -> rp.getPosition() < 4).count() == 2;
        for (RoomPlayer rp : roomPlayers) {
            boolean wonGame = false;

            final boolean isActivePlayer = rp.getPosition() < 4;
            final boolean isCurrentPlayerInRedTeam = this.isRedTeam(rp.getPosition());

            if (isActivePlayer) {
                PlayerReward playerReward = playerRewards.stream()
                        .filter(pr -> pr.getPlayerPosition() == rp.getPosition())
                        .findFirst()
                        .orElse(new PlayerReward(rp.getPosition()));

                if (isCurrentPlayerInRedTeam && this.getSetsRedTeam().get() == 2 || !isCurrentPlayerInRedTeam && this.getSetsBlueTeam().get() == 2) {
                    wonGame = true;
                }

                if (!isSingles)
                    playerReward.setActiveBonuses(playerReward.getActiveBonuses() | BonusIconHighlightValues.TeamBonus);

                ExpGoldBonus expGoldBonusSimple = new ExpGoldBonusImpl(playerReward.getExp(), playerReward.getGold());
                int rewardExpSimple = expGoldBonusSimple.calculateExp();
                int rewardGoldSimple = expGoldBonusSimple.calculateGold();

                // add house bonus
                ExpGoldBonus expGoldBonus = new BasicHouseBonus(expGoldBonusSimple, rp.getPlayer().getAccount().getId());
                int rewardExp = expGoldBonus.calculateExp();
                int rewardGold = expGoldBonus.calculateGold();

                if (rewardExpSimple != rewardExp || rewardGoldSimple != rewardGold) {
                    playerReward.setActiveBonuses(playerReward.getActiveBonuses() | BonusIconHighlightValues.HouseBonus);
                }

                // add exp, gold or wiseman ring bonus if equipped
                if (!rp.isRingOfWisemanEquipped()) {
                    if (rp.isRingOfExpEquipped()) {
                        expGoldBonus = new RingOfExpBonus(expGoldBonus);
                        rewardExp = expGoldBonus.calculateExp();
                        rewardGold = expGoldBonus.calculateGold();

                        playerReward.setActiveBonuses(playerReward.getActiveBonuses() | (BonusIconHighlightValues.ExpBonus << 16));
                    }
                    if (rp.isRingOfGoldEquipped()) {
                        expGoldBonus = new RingOfGoldBonus(expGoldBonus);
                        rewardExp = expGoldBonus.calculateExp();
                        rewardGold = expGoldBonus.calculateGold();

                        playerReward.setActiveBonuses(playerReward.getActiveBonuses() | (BonusIconHighlightValues.GoldBonus << 16));
                    }
                } else {
                    expGoldBonus = new RingOfWisemanBonus(expGoldBonus);
                    rewardExp = expGoldBonus.calculateExp();
                    rewardGold = expGoldBonus.calculateGold();

                    playerReward.setActiveBonuses(playerReward.getActiveBonuses() | (BonusIconHighlightValues.WisemanBonus << 16));
                }

                playerReward.setExp(rewardExp);
                playerReward.setGold(rewardGold);

                // add couple bonus
                Friend friend = rp.getCouple();
                if (friend != null) {
                    final boolean hasCoupleInTeam = roomPlayers.stream()
                            .filter(roomPlayer -> roomPlayer.getPosition() != rp.getPosition())
                            .anyMatch(roomPlayer -> {
                                final boolean isInRedTeam = this.isRedTeam(roomPlayer.getPosition());
                                final boolean isInBlueTeam = this.isBlueTeam(roomPlayer.getPosition());
                                if (isInRedTeam == isCurrentPlayerInRedTeam || isInBlueTeam == !isCurrentPlayerInRedTeam) {
                                    Friend f = roomPlayer.getCouple();
                                    return f != null && f.getFriend().getId().equals(friend.getPlayer().getId()) && f.getEFriendshipState() == friend.getEFriendshipState();
                                }
                                return false;
                            });
                    if (hasCoupleInTeam) {
                        int newCouplePoints = 0;
                        if (wonGame)
                            newCouplePoints = 5;
                        else
                            newCouplePoints = 2;

                        playerReward.setCouplePoints(newCouplePoints);
                        playerReward.setActiveBonuses(playerReward.getActiveBonuses() | (BonusIconHighlightValues.CoupleBonus << 8));
                    }
                }
            }
        }
    }

    private void resetPoints() {
        this.pointsRedTeam.set(0);
        this.pointsBlueTeam.set(0);
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
        final long outerFieldPosY = 125;
        final long innerFieldPosY = 75;

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

    @Override
    protected MatchplayHandleable createHandler() {
        return new MatchplayBasicModeHandler(this);
    }
}
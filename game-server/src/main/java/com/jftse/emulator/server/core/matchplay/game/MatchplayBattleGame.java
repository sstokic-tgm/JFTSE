package com.jftse.emulator.server.core.matchplay.game;

import com.jftse.emulator.server.core.constants.BonusIconHighlightValues;
import com.jftse.emulator.server.core.constants.GameFieldSide;
import com.jftse.emulator.server.core.jdbc.JdbcUtil;
import com.jftse.emulator.server.core.life.progression.ExpGoldBonus;
import com.jftse.emulator.server.core.life.progression.ExpGoldBonusImpl;
import com.jftse.emulator.server.core.life.progression.SimpleExpGoldBonus;
import com.jftse.emulator.server.core.life.progression.bonuses.*;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.core.matchplay.MatchplayHandleable;
import com.jftse.emulator.server.core.matchplay.PlayerReward;
import com.jftse.emulator.server.core.matchplay.combat.PlayerCombatSystem;
import com.jftse.emulator.server.core.matchplay.handler.MatchplayBattleModeHandler;
import com.jftse.emulator.server.core.utils.BattleUtils;
import com.jftse.entities.database.model.item.Product;
import com.jftse.entities.database.model.map.SMaps;
import com.jftse.entities.database.model.messenger.Friend;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.matchplay.battle.PlayerBattleState;
import com.jftse.server.core.matchplay.battle.SkillCrystal;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.TypedQuery;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Getter
@Setter
public class MatchplayBattleGame extends MatchplayGame {
    private AtomicLong crystalSpawnInterval;
    private AtomicLong crystalDeSpawnInterval;
    private List<Point> playerLocationsOnMap;
    private ConcurrentLinkedDeque<PlayerBattleState> playerBattleStates;
    private ConcurrentLinkedDeque<SkillCrystal> skillCrystals;
    private AtomicInteger lastCrystalId;
    private AtomicInteger lastGuardianServeSide;
    private AtomicInteger spiderMineIdentifier;

    private final PlayerCombatSystem playerCombatSystem;

    private SMaps map;

    private final JdbcUtil jdbcUtil;

    private final Random random = new Random();

    public MatchplayBattleGame() {
        super();

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        this.startTime = new AtomicReference<>(cal.getTime());

        this.crystalSpawnInterval = new AtomicLong(0);
        this.crystalDeSpawnInterval = new AtomicLong(0);
        this.playerLocationsOnMap = Arrays.asList(
                new Point(20, -125),
                new Point(-20, 125),
                new Point(-20, -75),
                new Point(20, 75));

        this.playerBattleStates = new ConcurrentLinkedDeque<>();
        this.skillCrystals = new ConcurrentLinkedDeque<>();
        this.lastCrystalId = new AtomicInteger(-1);
        this.lastGuardianServeSide = new AtomicInteger(GameFieldSide.RedTeam);
        this.willDamages = new ArrayList<>();
        this.spiderMineIdentifier = new AtomicInteger(0);
        this.scheduledFutures = new ConcurrentLinkedDeque<>();
        this.finished = new AtomicBoolean(false);

        playerCombatSystem = new PlayerCombatSystem(this);

        this.jdbcUtil = ServiceManager.getInstance().getJdbcUtil();
    }

    public PlayerBattleState createPlayerBattleState(RoomPlayer roomPlayer) {
        short baseHp = (short) BattleUtils.calculatePlayerHp(roomPlayer.getPlayer().getLevel());
        short baseStr = roomPlayer.getPlayer().getStrength();
        short baseSta = roomPlayer.getPlayer().getStamina();
        short baseDex = roomPlayer.getPlayer().getDexterity();
        short baseWill = roomPlayer.getPlayer().getWillpower();
        short totalHp = (short) (baseHp + roomPlayer.getStatusPointsAddedDto().getAddHp());
        short totalStr = (short) (baseStr + roomPlayer.getStatusPointsAddedDto().getStrength());
        short totalSta = (short) (baseSta + roomPlayer.getStatusPointsAddedDto().getStamina());
        short totalDex = (short) (baseDex + roomPlayer.getStatusPointsAddedDto().getDexterity());
        short totalWill = (short) (baseWill + roomPlayer.getStatusPointsAddedDto().getWillpower());
        return new PlayerBattleState(roomPlayer.getPosition(), roomPlayer.getPlayerId(), totalHp, totalStr, totalSta, totalDex, totalWill);
    }

    public List<Integer> getPlayerPositionsOrderedByHighestHealth() {
        List<Integer> playerPositions = new ArrayList<>();
        this.playerBattleStates.stream()
                .sorted(Comparator.comparingInt(pbs -> ((PlayerBattleState) pbs).getCurrentHealth().get())
                        .reversed())
                .forEach(p -> playerPositions.add(p.getPosition()));

        return playerPositions;
    }

    public List<PlayerReward> getPlayerRewards() {
        int secondsPlayed = (int) Math.ceil((double) this.getTimeNeeded() / 1000);
        List<PlayerReward> playerRewards = new ArrayList<>();

        final List<Product> mapRewards = new ArrayList<>();
        jdbcUtil.execute(em -> {
            TypedQuery<Product> queryProduct = em.createQuery("SELECT p FROM SRelationships sr LEFT JOIN FETCH Product p " +
                    "ON p.productIndex = sr.id_f " +
                    "WHERE sr.id_t = :mapId AND sr.status.id = 1 AND sr.relationship.id = 3 AND sr.role.id = 1", Product.class);
            queryProduct.setParameter("mapId", this.map.getId());
            List<Product> products = queryProduct.getResultList();

            mapRewards.addAll(products);
        });

        int iteration = 0;
        for (int playerPosition : this.getPlayerPositionsOrderedByHighestHealth()) {
            boolean wonGame = false;
            boolean isPlayerInRedTeam = this.isRedTeam(playerPosition);
            boolean allPlayersTeamRedDead = this.getPlayerBattleStates().stream()
                    .filter(x -> this.isRedTeam(x.getPosition()))
                    .allMatch(x -> x.getCurrentHealth().get() < 1);
            boolean allPlayersTeamBlueDead = this.getPlayerBattleStates().stream()
                    .filter(x -> this.isBlueTeam(x.getPosition()))
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

            ExpGoldBonus expGoldBonus = new ExpGoldBonusImpl(basicExpReward, basicExpReward);

            switch (iteration) {
                case 0 -> expGoldBonus = new SimpleExpGoldBonus(expGoldBonus, 0.1);
                case 1 -> expGoldBonus = new SimpleExpGoldBonus(expGoldBonus, 0.05);
            }

            if (wonGame) {
                expGoldBonus = new WonGameBonus(expGoldBonus);
            }

            int rewardExp = expGoldBonus.calculateExp();
            int rewardGold = expGoldBonus.calculateGold();
            PlayerReward playerReward = new PlayerReward(playerPosition);
            playerReward.setPlayerPosition(playerPosition);
            playerReward.setExp(rewardExp);
            playerReward.setGold(rewardGold);

            final int itemRewardToGive = random.nextInt(mapRewards.size());
            playerReward.setProductIndex(mapRewards.get(itemRewardToGive).getProductIndex());

            int min = 1;
            int max = !wonGame ? 2 : 3;
            int amount = random.nextInt(max - min + 1) + min;

            if (mapRewards.get(itemRewardToGive).getCategory().equals(EItemCategory.PARTS.getName()))
                amount = 1;

            if (mapRewards.get(itemRewardToGive).getCategory().equals(EItemCategory.QUICK.getName())) {
                min = 5;
                max = !wonGame ? 30 : 50;
                amount = random.nextInt(max - min + 1) + min;
            }

            playerReward.setProductAmount(amount);

            playerRewards.add(playerReward);

            iteration++;
        }

        return playerRewards;
    }

    @Override
    public void addBonusesToRewards(ConcurrentLinkedDeque<RoomPlayer> roomPlayers, List<PlayerReward> playerRewards) {
        final boolean isSingles = roomPlayers.stream().filter(rp -> rp.getPosition() < 4).count() == 2;

        final boolean allPlayersTeamRedDead = this.getPlayerBattleStates().stream()
                .filter(x -> this.isRedTeam(x.getPosition()))
                .allMatch(x -> x.getCurrentHealth().get() < 1);
        final boolean allPlayersTeamBlueDead = this.getPlayerBattleStates().stream()
                .filter(x -> this.isBlueTeam(x.getPosition()))
                .allMatch(x -> x.getCurrentHealth().get() < 1);

        for (RoomPlayer rp : roomPlayers) {
            boolean wonGame = false;

            final boolean isActivePlayer = rp.getPosition() < 4;
            final boolean isCurrentPlayerInRedTeam = this.isRedTeam(rp.getPosition());

            if (isActivePlayer) {
                PlayerReward playerReward = playerRewards.stream()
                        .filter(pr -> pr.getPlayerPosition() == rp.getPosition())
                        .findFirst()
                        .orElse(new PlayerReward(rp.getPosition()));

                if (isCurrentPlayerInRedTeam && allPlayersTeamBlueDead || !isCurrentPlayerInRedTeam && allPlayersTeamRedDead) {
                    wonGame = true;
                }

                if (!isSingles)
                    playerReward.setActiveBonuses(playerReward.getActiveBonuses() | BonusIconHighlightValues.TeamBonus);

                ExpGoldBonus expGoldBonusSimple = new ExpGoldBonusImpl(playerReward.getExp(), playerReward.getGold());
                int rewardExpSimple = expGoldBonusSimple.calculateExp();
                int rewardGoldSimple = expGoldBonusSimple.calculateGold();

                // add house bonus
                ExpGoldBonus expGoldBonus = new BattleHouseBonus(expGoldBonusSimple, rp.getPlayer().getAccount().getId());
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

    @Override
    protected MatchplayHandleable createHandler() {
        return new MatchplayBattleModeHandler(this);
    }
}

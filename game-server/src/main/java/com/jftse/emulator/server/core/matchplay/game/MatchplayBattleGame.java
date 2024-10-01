package com.jftse.emulator.server.core.matchplay.game;

import com.jftse.emulator.server.core.constants.BonusIconHighlightValues;
import com.jftse.emulator.server.core.constants.GameFieldSide;
import com.jftse.emulator.server.core.life.progression.ExpGoldBonus;
import com.jftse.emulator.server.core.life.progression.ExpGoldBonusImpl;
import com.jftse.emulator.server.core.life.progression.SimpleExpGoldBonus;
import com.jftse.emulator.server.core.life.progression.bonuses.*;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.core.matchplay.MatchplayHandleable;
import com.jftse.emulator.server.core.matchplay.MatchplayReward;
import com.jftse.emulator.server.core.matchplay.PlayerReward;
import com.jftse.emulator.server.core.matchplay.combat.PlayerCombatSystem;
import com.jftse.emulator.server.core.matchplay.handler.MatchplayBattleModeHandler;
import com.jftse.emulator.server.core.utils.BattleUtils;
import com.jftse.entities.database.model.SRelationships;
import com.jftse.entities.database.model.item.ItemEnchantLevel;
import com.jftse.entities.database.model.item.Product;
import com.jftse.entities.database.model.map.SMaps;
import com.jftse.entities.database.model.messenger.Friend;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.item.EElementalKind;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.jdbc.JdbcUtil;
import com.jftse.server.core.matchplay.*;
import com.jftse.server.core.matchplay.battle.PlayerBattleState;
import com.jftse.server.core.matchplay.battle.SkillCrystal;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.TypedQuery;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
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
    private final boolean isSingles;

    private final PlayerCombatSystem playerCombatSystem;

    private SMaps map;

    private final JdbcUtil jdbcUtil;

    private final Random random = new Random();

    public MatchplayBattleGame(byte players) {
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

        this.isSingles = players == 2;

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
        short totalStr = (short) (baseStr + roomPlayer.getStatusPointsAddedDto().getStrength() + roomPlayer.getStatusPointsAddedDto().getAddStr());
        short totalSta = (short) (baseSta + roomPlayer.getStatusPointsAddedDto().getStamina() + roomPlayer.getStatusPointsAddedDto().getAddSta());
        short totalDex = (short) (baseDex + roomPlayer.getStatusPointsAddedDto().getDexterity() + roomPlayer.getStatusPointsAddedDto().getAddDex());
        short totalWill = (short) (baseWill + roomPlayer.getStatusPointsAddedDto().getWillpower() + roomPlayer.getStatusPointsAddedDto().getAddWil());

        PlayerBattleState pbs = new PlayerBattleState(roomPlayer.getPosition(), roomPlayer.getPlayerId(), totalHp, totalStr, totalSta, totalDex, totalWill);

        Map<String, Integer> equipment = ServiceManager.getInstance().getClothEquipmentService().getEquippedCloths(roomPlayer.getPlayer());
        Pocket pocket = roomPlayer.getPlayer().getPocket();
        pocket = ServiceManager.getInstance().getPocketService().findById(pocket.getId());
        if (!equipment.isEmpty()) {
            Integer racketPlayerPocketId = equipment.get("racket");
            PlayerPocket offensivePP = ServiceManager.getInstance().getPlayerPocketService().getItemAsPocket(racketPlayerPocketId.longValue(), pocket);
            if (offensivePP != null) {
                Elementable element = getElementalProperties(offensivePP);
                pbs.setOffensiveElement(element);
            }
            final Pocket finalPocket = pocket;
            equipment.entrySet().stream()
                    .filter(e -> !e.getKey().equals("racket")) // we dont need offensive element here
                    .forEach(e -> {
                        Integer playerPocketId = e.getValue();
                        PlayerPocket pp = ServiceManager.getInstance().getPlayerPocketService().getItemAsPocket(playerPocketId.longValue(), finalPocket);
                        if (pp != null) {
                            Elementable element = getElementalProperties(pp);
                            if (element != null) {
                                pbs.getDefensiveElements().add(element);
                            }
                        }
                    });
        }

        return pbs;
    }

    private Elementable getElementalProperties(PlayerPocket pp) {
        return switch (pp.getEnchantElement()) {
            case 5 -> {
                ItemEnchantLevel itemEnchantLevel = ServiceManager.getInstance().getEnchantService().getItemEnchantLevel(EElementalKind.EARTH.getNameNormalized(), pp.getEnchantLevel());
                yield new EarthElement(itemEnchantLevel.getMinEfficiency(), itemEnchantLevel.getMaxEfficiency());
            }
            case 6 -> {
                ItemEnchantLevel itemEnchantLevel = ServiceManager.getInstance().getEnchantService().getItemEnchantLevel(EElementalKind.WIND.getNameNormalized(), pp.getEnchantLevel());
                yield new WindElement(itemEnchantLevel.getMinEfficiency(), itemEnchantLevel.getMaxEfficiency());
            }
            case 7 -> {
                ItemEnchantLevel itemEnchantLevel = ServiceManager.getInstance().getEnchantService().getItemEnchantLevel(EElementalKind.WATER.getNameNormalized(), pp.getEnchantLevel());
                yield new WaterElement(itemEnchantLevel.getMinEfficiency(), itemEnchantLevel.getMaxEfficiency());
            }
            case 8 -> {
                ItemEnchantLevel itemEnchantLevel = ServiceManager.getInstance().getEnchantService().getItemEnchantLevel(EElementalKind.FIRE.getNameNormalized(), pp.getEnchantLevel());
                yield new FireElement(itemEnchantLevel.getMinEfficiency(), itemEnchantLevel.getMaxEfficiency());
            }
            default -> null;
        };
    }

    public List<Integer> getPlayerPositionsOrderedByHighestHealth() {
        List<Integer> playerPositions = new ArrayList<>();
        this.playerBattleStates.stream()
                .sorted(Comparator.comparingInt(pbs -> ((PlayerBattleState) pbs).getCurrentHealth().get())
                        .reversed())
                .forEach(p -> playerPositions.add(p.getPosition()));

        return playerPositions;
    }

    public MatchplayReward getMatchRewards() {
        int secondsPlayed = (int) Math.ceil((double) this.getTimeNeeded() / 1000);
        final List<SRelationships> rewardRelationships = new ArrayList<>();
        final List<Product> mapRewards = new ArrayList<>();

        jdbcUtil.execute(em -> {
            TypedQuery<SRelationships> queryRelationships = em.createQuery("SELECT sr FROM SRelationships sr " +
                    "WHERE sr.id_t = :mapId AND sr.status.id = 1 AND sr.relationship.id = 3 AND sr.role.id = 1", SRelationships.class);
            queryRelationships.setParameter("mapId", this.map.getId());
            rewardRelationships.addAll(queryRelationships.getResultList());

            TypedQuery<Product> queryProduct = em.createQuery("SELECT p FROM SRelationships sr LEFT JOIN FETCH Product p " +
                    "ON p.productIndex = sr.id_f " +
                    "WHERE sr.id IN :relationshipIds", Product.class);
            final List<Long> relationshipIds = rewardRelationships.stream().map(SRelationships::getId).toList();
            queryProduct.setParameter("relationshipIds", relationshipIds);
            List<Product> products = queryProduct.getResultList();

            mapRewards.addAll(products);
        });

        MatchplayReward reward = new MatchplayReward();
        final List<MatchplayReward.ItemReward> itemRewards = new ArrayList<>();

        if (!mapRewards.isEmpty() && !rewardRelationships.isEmpty()) {
            for (Product product : mapRewards) {
                Double weight = rewardRelationships.stream()
                        .filter(x -> x.getId_f().intValue() == product.getProductIndex())
                        .findFirst()
                        .map(SRelationships::getWeight)
                        .orElse(null);
                Integer qty = rewardRelationships.stream()
                        .filter(x -> x.getId_f().intValue() == product.getProductIndex())
                        .findFirst()
                        .map(SRelationships::getQty)
                        .orElse(null);
                Integer qtyMin = rewardRelationships.stream()
                        .filter(x -> x.getId_f().intValue() == product.getProductIndex())
                        .findFirst()
                        .map(SRelationships::getQtyMin)
                        .orElse(null);
                Integer qtyMax = rewardRelationships.stream()
                        .filter(x -> x.getId_f().intValue() == product.getProductIndex())
                        .findFirst()
                        .map(SRelationships::getQtyMax)
                        .orElse(null);
                Integer levelReq = rewardRelationships.stream()
                        .filter(x -> x.getId_f().intValue() == product.getProductIndex())
                        .findFirst()
                        .map(SRelationships::getLevelReq)
                        .orElse(null);
                Boolean forDoubles = rewardRelationships.stream()
                        .filter(x -> x.getId_f().intValue() == product.getProductIndex())
                        .findFirst()
                        .map(SRelationships::getForDoubles)
                        .orElse(null);
                // Skip reward if it's exclusively for doubles or singles
                if (forDoubles != null) {
                    if ((this.isSingles && forDoubles) || (!this.isSingles && !forDoubles)) {
                        continue;  // Skip the reward if it's exclusive to the other match type
                    }
                }

                if (qty == null) {
                    if (qtyMin == null) {
                        if (product.getCategory().equals(EItemCategory.MATERIAL.getName()))
                            qtyMin = 1;
                        else if (product.getCategory().equals(EItemCategory.QUICK.getName()))
                            qtyMin = 5;
                        else if (product.getCategory().equals(EItemCategory.PARTS.getName()))
                            qtyMin = 1;
                        else
                            qtyMin = 1;
                    }
                    if (qtyMax == null) {
                        if (product.getCategory().equals(EItemCategory.MATERIAL.getName()))
                            qtyMax = 3;
                        else if (product.getCategory().equals(EItemCategory.QUICK.getName()))
                            qtyMax = 100;
                        else if (product.getCategory().equals(EItemCategory.PARTS.getName()))
                            qtyMax = 1;
                        else
                            qtyMax = 1;
                    }
                    qty = random.nextInt(qtyMax - qtyMin + 1) + qtyMin;
                } else {
                    if (qtyMin != null && qtyMax != null) {
                        qty = random.nextInt(qtyMax - qtyMin + 1) + qtyMin;
                    }
                }

                itemRewards.add(new MatchplayReward.ItemReward(product.getProductIndex(), qty, weight));
            }
        }

        // also allow room for no item reward
        itemRewards.add(new MatchplayReward.ItemReward(0, 0, 15.0));

        // if there is less than 4 items, add more items to reach 4
        /*if (itemRewards.size() < 4) {
            final int diff = 4 - itemRewards.size();
            for (int i = 0; i < diff; i++) {
                itemRewards.add(new MatchplayReward.ItemReward(0, 0, 15.0));
            }
        }*/

        double averageWeight = MatchplayReward.calculateAverageWeight(itemRewards);
        List<MatchplayReward.ItemReward> finalItemRewards = MatchplayReward.selectItemRewardsByWeight(itemRewards, 4, averageWeight);
        reward.addItemRewards(finalItemRewards);
        reward.assignItemRewardsToSlots(finalItemRewards);

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

            int basicExpReward = (int) Math.round(30 + (secondsPlayed - 90) * 0.12);
            int basicGoldReward = (int) Math.round(30 + (secondsPlayed - 90) * 0.12);

            basicExpReward = basicExpReward + (random.nextInt(401) + 800);
            basicGoldReward = basicGoldReward + (random.nextInt(401) + 700);

            ExpGoldBonus expGoldBonus = new ExpGoldBonusImpl(basicExpReward, basicGoldReward);

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

            reward.addPlayerReward(playerReward);

            iteration++;
        }

        return reward;
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

                rewardExp = (int) Math.round(rewardExp / 3.5);
                rewardGold = (int) Math.round(rewardGold / 3.5);

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

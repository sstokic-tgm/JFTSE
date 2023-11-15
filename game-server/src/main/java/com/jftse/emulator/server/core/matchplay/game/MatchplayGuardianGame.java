package com.jftse.emulator.server.core.matchplay.game;

import com.jftse.emulator.server.core.constants.BonusIconHighlightValues;
import com.jftse.emulator.server.core.constants.GameFieldSide;
import com.jftse.emulator.server.core.jdbc.JdbcUtil;
import com.jftse.emulator.server.core.life.progression.ExpGoldBonus;
import com.jftse.emulator.server.core.life.progression.ExpGoldBonusImpl;
import com.jftse.emulator.server.core.life.progression.bonuses.BattleHouseBonus;
import com.jftse.emulator.server.core.life.progression.bonuses.RingOfExpBonus;
import com.jftse.emulator.server.core.life.progression.bonuses.RingOfGoldBonus;
import com.jftse.emulator.server.core.life.progression.bonuses.RingOfWisemanBonus;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.core.matchplay.MatchplayHandleable;
import com.jftse.emulator.server.core.matchplay.PlayerReward;
import com.jftse.emulator.server.core.matchplay.combat.GuardianCombatSystem;
import com.jftse.emulator.server.core.matchplay.combat.PlayerCombatSystem;
import com.jftse.emulator.server.core.matchplay.handler.MatchplayGuardianModeHandler;
import com.jftse.emulator.server.core.utils.BattleUtils;
import com.jftse.entities.database.model.battle.*;
import com.jftse.entities.database.model.item.Product;
import com.jftse.entities.database.model.map.SMaps;
import com.jftse.entities.database.model.messenger.Friend;
import com.jftse.entities.database.model.scenario.MScenarios;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.matchplay.battle.GuardianBattleState;
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
import java.util.stream.Collectors;

@Getter
@Setter
public class MatchplayGuardianGame extends MatchplayGame {
    private final short guardianHealPercentage = 5; // Balancing purposes. Only ever heal 5% of guardians hp.
    public final static long guardianAttackLoopTime = TimeUnit.SECONDS.toMillis(8);

    private AtomicLong crystalSpawnInterval;
    private AtomicLong crystalDeSpawnInterval;
    private List<Point> playerLocationsOnMap;
    private ConcurrentLinkedDeque<PlayerBattleState> playerBattleStates;
    private ConcurrentLinkedDeque<GuardianBattleState> guardianBattleStates;
    private ConcurrentLinkedDeque<SkillCrystal> skillCrystals;
    private List<WillDamage> willDamages;
    private AtomicInteger lastCrystalId;
    private AtomicBoolean bossBattleActive;
    private AtomicInteger lastGuardianServeSide;
    private AtomicInteger guardianLevelLimit;
    private AtomicReference<Date> stageStartTime;
    private AtomicInteger expPot;
    private AtomicInteger goldPot;
    private AtomicBoolean isHardMode;
    private AtomicBoolean isRandomGuardiansMode;
    private AtomicInteger spiderMineIdentifier;

    private AtomicBoolean stageChangingToBoss;

    private MScenarios scenario;
    private SMaps map;
    private List<Guardian2Maps> guardiansInStage;
    private List<Guardian2Maps> guardiansInBossStage;

    private final PlayerCombatSystem playerCombatSystem;
    private final GuardianCombatSystem guardianCombatSystem;

    private final Random random;

    public MatchplayGuardianGame() {
        super();

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        this.startTime = new AtomicReference<>(cal.getTime());
        this.stageStartTime = new AtomicReference<>(cal.getTime());
        this.playerLocationsOnMap = Arrays.asList(
                new Point(20, -75),
                new Point(-20, -75));

        this.playerBattleStates = new ConcurrentLinkedDeque<>();
        this.guardianBattleStates = new ConcurrentLinkedDeque<>();
        this.skillCrystals = new ConcurrentLinkedDeque<>();
        this.lastCrystalId = new AtomicInteger(-1);
        this.willDamages = new ArrayList<>();
        this.lastGuardianServeSide = new AtomicInteger(GameFieldSide.Guardian);
        this.scheduledFutures = new ConcurrentLinkedDeque<>();
        this.finished = new AtomicBoolean(false);
        this.crystalSpawnInterval = new AtomicLong(0);
        this.crystalDeSpawnInterval = new AtomicLong(0);
        this.guardianLevelLimit = new AtomicInteger(0);
        this.bossBattleActive = new AtomicBoolean(false);
        this.expPot = new AtomicInteger(0);
        this.goldPot = new AtomicInteger(0);
        this.isHardMode = new AtomicBoolean(false);
        this.isRandomGuardiansMode = new AtomicBoolean(false);
        this.spiderMineIdentifier = new AtomicInteger(0);
        this.stageChangingToBoss = new AtomicBoolean(false);

        playerCombatSystem = new PlayerCombatSystem(this);
        guardianCombatSystem = new GuardianCombatSystem(this);

        this.guardiansInStage = new ArrayList<>();
        this.guardiansInBossStage = new ArrayList<>();

        random = new Random();
    }

    public List<GuardianBase> determineGuardians(List<Guardian2Maps> guardian2Maps, int guardianLevelLimit) {
        List<GuardianBase> guardiansLeft = this.getFilteredGuardians(guardian2Maps, guardianLevelLimit, Guardian2Maps.Side.LEFT);
        List<GuardianBase> guardiansRight = this.getFilteredGuardians(guardian2Maps, guardianLevelLimit, Guardian2Maps.Side.RIGHT);
        List<GuardianBase> guardiansMiddle = this.getFilteredGuardians(guardian2Maps, guardianLevelLimit, Guardian2Maps.Side.MIDDLE);

        GuardianBase leftGuardian = this.getRandomGuardian(guardiansLeft, new ArrayList<>());
        GuardianBase rightGuardian = this.getRandomGuardian(guardiansRight, List.of(leftGuardian));
        GuardianBase middleGuardian = this.getRandomGuardian(guardiansMiddle, Arrays.asList(leftGuardian, rightGuardian));
        if (middleGuardian != null) {
            return Arrays.asList(middleGuardian, rightGuardian, leftGuardian);
        } else if (rightGuardian != null) {
            return Arrays.asList(rightGuardian, leftGuardian, null);
        }

        return Arrays.asList(leftGuardian, null, null);
    }

    public GuardianBase getRandomGuardian(List<GuardianBase> guardians, List<GuardianBase> guardiansToIgnore) {
        if (guardians.isEmpty()) {
            return null;
        }

        List<Long> idToIgnore = guardiansToIgnore.stream()
                .map(GuardianBase::getId)
                .toList();

        List<GuardianBase> filteredGuardianIds = guardians.stream()
                .filter(x -> !idToIgnore.contains(x.getId()))
                .toList();

        if (filteredGuardianIds.isEmpty()) {
            return null;
        }

        int randomIndex = random.nextInt(filteredGuardianIds.size());
        return filteredGuardianIds.get(randomIndex);
    }

    public List<GuardianBase> getFilteredGuardians(List<Guardian2Maps> guardian2Maps, int guardianLevelLimit, Guardian2Maps.Side side) {
        if (guardian2Maps.isEmpty()) {
            return new ArrayList<>();
        }

        final List<GuardianBase> guardians = new ArrayList<>();
        for (Guardian2Maps guardian2Map : guardian2Maps) {
            if (guardian2Map.getSide() != side) {
                continue;
            }
            if (guardian2Map.getGuardian() == null && guardian2Map.getBossGuardian() == null) {
                continue;
            }

            if (guardian2Map.getGuardian() != null) {
                Guardian guardian = ServiceManager.getInstance().getGuardianService().findGuardianById(guardian2Map.getGuardian().getId());
                if (guardian != null) {
                    guardians.add(guardian);
                }
            }
            if (guardian2Map.getBossGuardian() != null) {
                BossGuardian bossGuardian = ServiceManager.getInstance().getBossGuardianService().findBossGuardianById(guardian2Map.getBossGuardian().getId());
                if (bossGuardian != null) {
                    guardians.add(bossGuardian);
                }
            }
        }

        int lowestGuardianLevel = guardians.stream()
                .mapToInt(GuardianBase::getLevel)
                .min()
                .orElse(0);
        if (guardianLevelLimit < lowestGuardianLevel) {
            guardianLevelLimit = lowestGuardianLevel;
        }

        final int finalGuardianLevelLimit = guardianLevelLimit;
        return guardians.stream()
                .filter(x -> x.getLevel() <= finalGuardianLevelLimit)
                .collect(Collectors.toList());
    }

    public void fillRemainingGuardianSlots(boolean forceFill, MatchplayGuardianGame game, List<Guardian2Maps> guardian2Maps, List<GuardianBase> guardians) {
        int totalAvailableGuardianSlots = 3;
        int activeGuardianSlots = forceFill ? 0 : (int) guardians.stream().filter(Objects::nonNull).count();
        int remainingGuardianSlots = totalAvailableGuardianSlots - activeGuardianSlots;
        if (game.getIsHardMode().get() && remainingGuardianSlots != 0) {
            List<GuardianBase> allGuardians = getAllGuardiansFromGuardian2Maps(guardian2Maps);
            for (int i = activeGuardianSlots; i < totalAvailableGuardianSlots; i++) {
                guardians.set(i, getRandomGuardian(allGuardians, guardians));
            }
        }
    }

    public List<GuardianBase> getAllGuardiansFromGuardian2Maps(List<Guardian2Maps> guardian2Maps) {
        final List<GuardianBase> guardians = new ArrayList<>();
        for (Guardian2Maps guardian2Map : guardian2Maps) {
            if (guardian2Map.getGuardian() == null && guardian2Map.getBossGuardian() == null) {
                continue;
            }

            if (guardian2Map.getGuardian() != null) {
                Guardian guardian = ServiceManager.getInstance().getGuardianService().findGuardianById(guardian2Map.getGuardian().getId());
                if (guardian != null) {
                    guardians.add(guardian);
                }
            }
            if (guardian2Map.getBossGuardian() != null) {
                BossGuardian bossGuardian = ServiceManager.getInstance().getBossGuardianService().findBossGuardianById(guardian2Map.getBossGuardian().getId());
                if (bossGuardian != null) {
                    guardians.add(bossGuardian);
                }
            }
        }
        return guardians;
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
        return new PlayerBattleState(roomPlayer.getPosition(), totalHp, totalStr, totalSta, totalDex, totalWill);
    }

    public GuardianBattleState createGuardianBattleState(boolean isHardMode, GuardianBase guardian, short guardianPosition, int activePlayingPlayersCount) {
        if (isHardMode) {
            return new GuardianBattleState(guardian, guardianPosition, 8000, 110, 45, 165, 120, guardian.getRewardExp(), guardian.getRewardGold(), guardian.getRewardRankingPoint());
        }

        int extraHp = guardian.getHpPer() * activePlayingPlayersCount;
        int extraStr = guardian.getAddStr() * activePlayingPlayersCount;
        int extraSta = guardian.getAddSta() * activePlayingPlayersCount;
        int extraDex = guardian.getAddDex() * activePlayingPlayersCount;
        int extraWill = guardian.getAddWill() * activePlayingPlayersCount;
        int totalHp = guardian.getHpBase().shortValue() + extraHp;
        int totalStr = guardian.getBaseStr() + extraStr;
        int totalSta = guardian.getBaseSta() + extraSta;
        int totalDex = guardian.getBaseDex() + extraDex;
        int totalWill = guardian.getBaseWill() + extraWill;
        return new GuardianBattleState(guardian, guardianPosition, totalHp, totalStr, totalSta, totalDex, totalWill, guardian.getRewardExp(), guardian.getRewardGold(), guardian.getRewardRankingPoint());
    }

    public long getStageTimePlayingInSeconds() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        long duration = cal.getTime().getTime() - this.getStageStartTime().get().getTime();
        return TimeUnit.MILLISECONDS.toSeconds(duration);
    }

    public void resetStageStartTime() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        this.stageStartTime.set(cal.getTime());
        this.startTime.set(cal.getTime());
    }

    public List<PlayerReward> getPlayerRewards() {
        final boolean isBoss = map.getIsBossStage() && bossBattleActive.get();

        JdbcUtil jdbcUtil = ServiceManager.getInstance().getJdbcUtil();

        final List<Integer> lootedGuardians = this.guardianBattleStates.stream()
                .filter(x -> x.getCurrentHealth().get() < 1 && x.getLooted().get())
                .map(GuardianBattleState::getId)
                .toList();

        final List<Guardian2Maps> guardian2Maps = this.guardiansInStage.stream()
                .filter(x -> x.getGuardian() != null && lootedGuardians.contains(x.getGuardian().getId().intValue()))
                .filter(x -> x.getBossGuardian() != null && lootedGuardians.contains(x.getBossGuardian().getId().intValue()))
                .toList();

        final List<Guardian2Maps> guardian2MapsBoss = new ArrayList<>();
        if (isBoss) {
            final List<Guardian2Maps> tmp = this.guardiansInBossStage.stream()
                    .filter(x -> x.getGuardian() != null && lootedGuardians.contains(x.getGuardian().getId().intValue()))
                    .filter(x -> x.getBossGuardian() != null && lootedGuardians.contains(x.getBossGuardian().getId().intValue()))
                    .toList();
            guardian2MapsBoss.addAll(tmp);
        }

        final List<Product> stageRewards = new ArrayList<>();
        final List<SGuardianMultiplier> expMultipliers = new ArrayList<>();
        final List<SGuardianMultiplier> goldMultipliers = new ArrayList<>();

        jdbcUtil.execute(em -> {
            TypedQuery<SGuardianMultiplier> queryExp = em.createQuery("SELECT sgm FROM SRelationships sr " +
                    "LEFT JOIN FETCH SGuardianMultiplier sgm ON sgm.id = sr.id_f " +
                    "WHERE sr.id_t = :mapId AND sr.status.id = 1 AND sr.relationship.id = 6 AND sr.role.id = 2", SGuardianMultiplier.class);
            queryExp.setParameter("mapId", this.map.getId());
            expMultipliers.addAll(queryExp.getResultList());

            TypedQuery<SGuardianMultiplier> queryGold = em.createQuery("SELECT sgm FROM SRelationships sr " +
                    "LEFT JOIN FETCH SGuardianMultiplier sgm ON sgm.id = sr.id_f " +
                    "WHERE sr.id_t = :mapId AND sr.status.id = 1 AND sr.relationship.id = 6 AND sr.role.id = 3", SGuardianMultiplier.class);
            queryGold.setParameter("mapId", this.map.getId());
            goldMultipliers.addAll(queryGold.getResultList());

            TypedQuery<Product> queryProduct = em.createQuery("SELECT p FROM SRelationships sr LEFT JOIN FETCH Product p " +
                    "ON p.productIndex = sr.id_f " +
                    "WHERE sr.id_t = :mapId AND sr.status.id = 1 AND sr.relationship.id = 7 AND sr.role.id = 1", Product.class);
            queryProduct.setParameter("mapId", this.map.getId());
            List<Product> products = queryProduct.getResultList();

            stageRewards.addAll(products);

            String qlGetGuardiansForProducts =
                    "SELECT p FROM SRelationships sr LEFT JOIN FETCH Product p " +
                    "ON p.productIndex = sr.id_f " +
                    "WHERE sr.id_t IN :guardianList AND sr.status.id = 1 AND sr.relationship.id IN (1,2) AND sr.role.id = 1";

            queryProduct = em.createQuery(qlGetGuardiansForProducts, Product.class);
            final List<Long> guardian2MapIds = guardian2Maps.stream()
                    .map(Guardian2Maps::getId)
                    .toList();
            queryProduct.setParameter("guardianList", guardian2MapIds);
            List<Product> guardianProducts = queryProduct.getResultList();

            if (isBoss) {
                queryProduct = em.createQuery(qlGetGuardiansForProducts, Product.class);
                final List<Long> guardian2MapIdsBoss = guardian2MapsBoss.stream()
                        .map(Guardian2Maps::getId)
                        .toList();
                queryProduct.setParameter("guardianList", guardian2MapIdsBoss);
                final List<Product> tmp = queryProduct.getResultList();

                guardianProducts.addAll(tmp);
            }
            stageRewards.addAll(guardianProducts);
        });

        SGuardianMultiplier sExpMultiplier = expMultipliers.isEmpty() ? null : expMultipliers.get(0);
        SGuardianMultiplier sGoldMultiplier = goldMultipliers.isEmpty() ? null : goldMultipliers.get(0);

        List<PlayerReward> playerRewards = new ArrayList<>();

        final boolean stageChangingToBoss = this.stageChangingToBoss.get();
        final boolean allGuardiansDead = this.guardianBattleStates.stream().allMatch(x -> x.getCurrentHealth().get() < 1);
        final boolean wonGame = allGuardiansDead && finished.get() && !stageChangingToBoss;

        this.playerBattleStates.forEach(x -> {
            PlayerReward playerReward = new PlayerReward(x.getPosition());
            playerReward.setPlayerPosition(x.getPosition());

            double expMultiplier = sExpMultiplier == null ? 1 : sExpMultiplier.getMultiplier();
            double goldMultiplier = sGoldMultiplier == null ? 1 : sGoldMultiplier.getMultiplier();
            playerReward.setExp((int) (this.getExpPot().get() * expMultiplier));
            playerReward.setGold((int) (this.getGoldPot().get() * goldMultiplier));
            playerReward.setProductIndex(-1);

            if (!stageRewards.isEmpty()) {
                if (wonGame) {
                    if (isBoss)
                        stageRewards.removeIf(p -> !p.getCategory().equals(EItemCategory.LOTTERY.getName()) || !p.getCategory().equals(EItemCategory.PARTS.getName()));

                    if (!stageRewards.isEmpty()) {
                        final int itemRewardToGive = random.nextInt(stageRewards.size());
                        playerReward.setProductIndex(stageRewards.get(itemRewardToGive).getProductIndex());

                        int amount = this.getIsHardMode().get() ? 3 : 1;
                        if (this.getIsRandomGuardiansMode().get())
                            amount = 1;

                        if (stageRewards.get(itemRewardToGive).getCategory().equals(EItemCategory.PARTS.getName()))
                            amount = 1;

                        if (stageRewards.get(itemRewardToGive).getCategory().equals(EItemCategory.MATERIAL.getName())) {
                            final int min = 1;
                            final int max = 3;
                            amount = random.nextInt(max - min + 1) + min;
                        }

                        if (stageRewards.get(itemRewardToGive).getCategory().equals(EItemCategory.QUICK.getName())) {
                            final int min = 5;
                            final int max = 100;
                            amount = random.nextInt(max - min + 1) + min;
                        }
                        playerReward.setProductAmount(amount);
                    }
                } else {
                    final List<Product> loosingStageRewards = stageRewards.stream()
                            .filter(p -> p.getCategory().equals(EItemCategory.MATERIAL.getName()) || p.getCategory().equals(EItemCategory.QUICK.getName()))
                            .toList();

                    if (!loosingStageRewards.isEmpty()) {
                        final int itemRewardToGiveLoosing = random.nextInt(loosingStageRewards.size());
                        playerReward.setProductIndex(loosingStageRewards.get(itemRewardToGiveLoosing).getProductIndex());

                        final int min = 1;
                        final int max = 2;
                        int amount = random.nextInt(max - min + 1) + min;

                        if (loosingStageRewards.get(itemRewardToGiveLoosing).getCategory().equals(EItemCategory.QUICK.getName())) {
                            final int minQuick = 5;
                            final int maxQuick = 50;
                            amount = random.nextInt(maxQuick - minQuick + 1) + minQuick;
                        }

                        playerReward.setProductAmount(amount);
                    }
                }
            }

            if (!wonGame) {
                playerReward.setExp(playerReward.getExp() / 2);
                playerReward.setGold(playerReward.getGold() / 2);
            }

            playerRewards.add(playerReward);
        });

        return playerRewards;
    }

    @Override
    public void addBonusesToRewards(ConcurrentLinkedDeque<RoomPlayer> roomPlayers, List<PlayerReward> playerRewards) {
        final boolean allPlayersDead = getPlayerBattleStates().stream().allMatch(x -> x.getCurrentHealth().get() < 1);
        final boolean allGuardiansDead = getGuardianBattleStates().stream().allMatch(x -> x.getCurrentHealth().get() < 1);
        final boolean wonGame = allGuardiansDead && !allPlayersDead;

        for (RoomPlayer rp : roomPlayers) {
            final boolean isActivePlayer = rp.getPosition() < 4;
            final boolean isCurrentPlayerInRedTeam = this.isRedTeam(rp.getPosition());

            if (isActivePlayer) {
                PlayerReward playerReward = playerRewards.stream()
                        .filter(pr -> pr.getPlayerPosition() == rp.getPosition())
                        .findFirst()
                        .orElse(new PlayerReward(rp.getPosition()));

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
                                if (isInRedTeam == isCurrentPlayerInRedTeam) {
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
    public boolean isRedTeam(int playerPos) {
        return true;
    }

    @Override
    public boolean isBlueTeam(int playerPos) {
        return false;
    }

    @Override
    protected MatchplayHandleable createHandler() {
        return new MatchplayGuardianModeHandler(this);
    }
}

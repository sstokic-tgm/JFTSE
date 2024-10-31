package com.jftse.emulator.server.core.matchplay.game;

import com.jftse.emulator.common.scripting.ScriptFile;
import com.jftse.emulator.common.scripting.ScriptManager;
import com.jftse.emulator.common.scripting.ScriptManagerFactory;
import com.jftse.emulator.server.core.constants.BonusIconHighlightValues;
import com.jftse.emulator.server.core.constants.GameFieldSide;
import com.jftse.emulator.server.core.life.progression.ExpGoldBonus;
import com.jftse.emulator.server.core.life.progression.ExpGoldBonusImpl;
import com.jftse.emulator.server.core.life.progression.bonuses.BattleHouseBonus;
import com.jftse.emulator.server.core.life.progression.bonuses.RingOfExpBonus;
import com.jftse.emulator.server.core.life.progression.bonuses.RingOfGoldBonus;
import com.jftse.emulator.server.core.life.progression.bonuses.RingOfWisemanBonus;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.core.matchplay.MatchplayHandleable;
import com.jftse.emulator.server.core.matchplay.MatchplayReward;
import com.jftse.emulator.server.core.matchplay.PlayerReward;
import com.jftse.emulator.server.core.matchplay.combat.GuardianCombatSystem;
import com.jftse.emulator.server.core.matchplay.combat.PlayerCombatSystem;
import com.jftse.emulator.server.core.matchplay.guardian.AdvancedGuardianState;
import com.jftse.emulator.server.core.matchplay.guardian.BossBattlePhaseable;
import com.jftse.emulator.server.core.matchplay.guardian.PhaseManager;
import com.jftse.emulator.server.core.matchplay.handler.MatchplayGuardianModeHandler;
import com.jftse.emulator.server.core.utils.BattleUtils;
import com.jftse.entities.database.model.SRelationships;
import com.jftse.entities.database.model.battle.*;
import com.jftse.entities.database.model.item.ItemEnchantLevel;
import com.jftse.entities.database.model.item.Product;
import com.jftse.entities.database.model.map.SMaps;
import com.jftse.entities.database.model.messenger.Friend;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.entities.database.model.scenario.MScenarios;
import com.jftse.server.core.item.EElementalKind;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.jdbc.JdbcUtil;
import com.jftse.server.core.matchplay.*;
import com.jftse.server.core.matchplay.battle.GuardianBattleState;
import com.jftse.server.core.matchplay.battle.PlayerBattleState;
import com.jftse.server.core.matchplay.battle.SkillCrystal;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import javax.persistence.TypedQuery;
import javax.script.Bindings;
import javax.script.ScriptContext;
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
@Log4j2
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

    private boolean isAdvancedBossGuardianMode;
    private PhaseManager phaseManager;

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

        this.isAdvancedBossGuardianMode = false;

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
                .filter(Objects::nonNull)
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
            if (guardian2Map.getGuardian() != null && guardian2Map.getSide() == side) {
                Guardian guardian = ServiceManager.getInstance().getGuardianService().findGuardianById(guardian2Map.getGuardian().getId());
                if (guardian != null) {
                    guardians.add(guardian);
                }
            }
            if (guardian2Map.getBossGuardian() != null && guardian2Map.getSide() == side) {
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

        List<GuardianBase> guardiansToReturn = guardians.stream()
                .filter(x -> x.getLevel() <= finalGuardianLevelLimit)
                .collect(Collectors.toList());

        if (guardiansToReturn.size() == 1) {
            guardians.stream()
                    .filter(g -> g.getId() > guardiansToReturn.getFirst().getId())
                    .findFirst()
                    .ifPresent(guardiansToReturn::add);

            // fallback
            if (guardiansToReturn.size() == 1) {
                guardians.stream()
                        .filter(g -> g.getId() < guardiansToReturn.getFirst().getId())
                        .findFirst()
                        .ifPresent(guardiansToReturn::add);
            }
        }

        return guardiansToReturn;
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

    public GuardianBattleState createGuardianBattleState(boolean isHardMode, GuardianBase guardian, short guardianPosition, int activePlayingPlayersCount) {
        if (isHardMode && !isAdvancedBossGuardianMode) {
            GuardianBattleState gbs = new GuardianBattleState(guardian, guardianPosition, 8000, 110, 45, 165, 120, guardian.getRewardExp(), guardian.getRewardGold(), guardian.getRewardRankingPoint());
            addElementsToGuardian(guardian, gbs.getElements());

            return gbs;
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

        if (isAdvancedBossGuardianMode) {
            AdvancedGuardianState advancedGuardianState = !isHardMode ?
                    new AdvancedGuardianState(map.getId(), scenario.getId(), guardian, guardianPosition, totalHp, 110, 45, 165, 120, guardian.getRewardExp(), guardian.getRewardGold(), guardian.getRewardRankingPoint())
                    : new AdvancedGuardianState(map.getId(), scenario.getId(), guardian, guardianPosition, 8000, 110, 45, 165, 120, guardian.getRewardExp(), guardian.getRewardGold(), guardian.getRewardRankingPoint());
            if (activePlayingPlayersCount == 4) {
                final int hp = advancedGuardianState.getCurrentHealth().get();
                final int newHp = (int) (hp * 1.5);
                advancedGuardianState.getCurrentHealth().set(newHp);
                advancedGuardianState.setMaxHealth(newHp);
            }

            addElementsToGuardian(guardian, advancedGuardianState.getElements());

            advancedGuardianState.loadSkills();
            return advancedGuardianState;
        }

        GuardianBattleState gbs = new GuardianBattleState(guardian, guardianPosition, totalHp, totalStr, totalSta, totalDex, totalWill, guardian.getRewardExp(), guardian.getRewardGold(), guardian.getRewardRankingPoint());
        addElementsToGuardian(guardian, gbs.getElements());

        return gbs;
    }

    private void addElementsToGuardian(GuardianBase guardian, List<Elementable> elements) {
        if (guardian.getElementGrade() == null || guardian.getElementGrade() == 0) {
            return;
        }

        if (guardian.getEarth() != null && guardian.getEarth()) {
            ItemEnchantLevel itemEnchantLevel = getItemEnchantLevel(EElementalKind.EARTH, guardian.getElementGrade());
            elements.add(new EarthElement(itemEnchantLevel.getMinEfficiency() + 20.0, itemEnchantLevel.getMaxEfficiency() + 20.0));
        }
        if (guardian.getWind() != null && guardian.getWind()) {
            ItemEnchantLevel itemEnchantLevel = getItemEnchantLevel(EElementalKind.WIND, guardian.getElementGrade());
            elements.add(new WindElement(itemEnchantLevel.getMinEfficiency() + 20.0, itemEnchantLevel.getMaxEfficiency() + 20.0));
        }
        if (guardian.getWater() != null && guardian.getWater()) {
            ItemEnchantLevel itemEnchantLevel = getItemEnchantLevel(EElementalKind.WATER, guardian.getElementGrade());
            elements.add(new WaterElement(itemEnchantLevel.getMinEfficiency() + 20.0, itemEnchantLevel.getMaxEfficiency() + 20.0));
        }
        if (guardian.getFire() != null && guardian.getFire()) {
            ItemEnchantLevel itemEnchantLevel = getItemEnchantLevel(EElementalKind.FIRE, guardian.getElementGrade());
            elements.add(new FireElement(itemEnchantLevel.getMinEfficiency() + 20.0, itemEnchantLevel.getMaxEfficiency() + 20.0));
        }
    }

    private ItemEnchantLevel getItemEnchantLevel(EElementalKind kind, int level) {
        return ServiceManager.getInstance().getEnchantService().getItemEnchantLevel(kind.getNameNormalized(), level);
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

    public MatchplayReward getMatchRewards() {
        final boolean isBoss = map.getIsBossStage() && bossBattleActive.get();

        JdbcUtil jdbcUtil = ServiceManager.getInstance().getJdbcUtil();

        final List<Integer> lootedGuardians = this.guardianBattleStates.stream()
                .filter(x -> x.getCurrentHealth().get() < 1 && x.getLooted().get())
                .map(GuardianBattleState::getId)
                .toList();

        final List<Guardian2Maps> guardian2Maps = this.guardiansInStage.stream()
                .filter(x -> (x.getGuardian() != null && lootedGuardians.contains(x.getGuardian().getId().intValue())) || (x.getBossGuardian() != null && lootedGuardians.contains(x.getBossGuardian().getId().intValue())))
                .toList();

        final List<Guardian2Maps> guardian2MapsBoss = new ArrayList<>();
        if (isBoss) {
            final List<Guardian2Maps> tmp = this.guardiansInBossStage.stream()
                    .filter(x -> (x.getGuardian() != null && lootedGuardians.contains(x.getGuardian().getId().intValue())) || (x.getBossGuardian() != null && lootedGuardians.contains(x.getBossGuardian().getId().intValue())))
                    .toList();
            guardian2MapsBoss.addAll(tmp);
        }

        final List<Product> stageRewards = new ArrayList<>();
        final List<SRelationships> stageRewardsRelationships = new ArrayList<>();
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

            TypedQuery<SRelationships> queryRelationships = em.createQuery("SELECT sr FROM SRelationships sr " +
                    "WHERE sr.id_t = :mapId AND sr.status.id = 1 AND sr.relationship.id = 7 AND sr.role.id = 1", SRelationships.class);
            queryRelationships.setParameter("mapId", this.map.getId());
            List<SRelationships> relationships = queryRelationships.getResultList();
            stageRewardsRelationships.addAll(relationships);

            TypedQuery<Product> queryProduct = em.createQuery("SELECT p FROM SRelationships sr LEFT JOIN FETCH Product p " +
                    "ON p.productIndex = sr.id_f " +
                    "WHERE sr.id IN :relationshipIds", Product.class);
            final List<Long> relationshipIds = relationships.stream().map(SRelationships::getId).toList();
            queryProduct.setParameter("relationshipIds", relationshipIds);
            List<Product> products = queryProduct.getResultList();
            stageRewards.addAll(products);

            queryRelationships = em.createQuery("SELECT sr FROM SRelationships sr " +
                    "WHERE sr.id_t IN :guardianList AND sr.status.id = 1 AND sr.relationship.id IN (1,2) AND sr.role.id = 1", SRelationships.class);
            final List<Long> guardian2MapIds = guardian2Maps.stream()
                    .map(Guardian2Maps::getId)
                    .toList();
            queryRelationships.setParameter("guardianList", guardian2MapIds);
            relationships = queryRelationships.getResultList();

            String qlGetGuardiansForProducts =
                    "SELECT p FROM SRelationships sr LEFT JOIN FETCH Product p " +
                    "ON p.productIndex = sr.id_f " +
                    "WHERE sr.id IN :relationshipIds";
            queryProduct = em.createQuery(qlGetGuardiansForProducts, Product.class);
            final List<Long> relationshipIdsGuardians = relationships.stream().map(SRelationships::getId).toList();
            queryProduct.setParameter("relationshipIds", relationshipIdsGuardians);
            List<Product> guardianProducts = queryProduct.getResultList();

            if (isBoss) {
                queryRelationships = em.createQuery("SELECT sr FROM SRelationships sr " +
                        "WHERE sr.id_t IN :guardianList AND sr.status.id = 1 AND sr.relationship.id IN (1,2) AND sr.role.id = 1", SRelationships.class);
                final List<Long> guardian2MapIdsBoss = guardian2MapsBoss.stream().map(Guardian2Maps::getId).toList();
                queryRelationships.setParameter("guardianList", guardian2MapIdsBoss);
                List<SRelationships> relationshipsBoss = queryRelationships.getResultList();

                queryProduct = em.createQuery(qlGetGuardiansForProducts, Product.class);
                final List<Long> relationshipIdsGuardiansBoss = relationshipsBoss.stream().map(SRelationships::getId).toList();
                queryProduct.setParameter("relationshipIds", relationshipIdsGuardiansBoss);
                final List<Product> tmp = queryProduct.getResultList();

                guardianProducts.addAll(tmp);
                relationships.addAll(relationshipsBoss);
            }
            stageRewards.addAll(guardianProducts);
            stageRewardsRelationships.addAll(relationships);
        });

        MatchplayReward reward = new MatchplayReward();
        final List<MatchplayReward.ItemReward> itemRewards = new ArrayList<>();

        final boolean stageChangingToBoss = this.stageChangingToBoss.get();
        final boolean allGuardiansDead = this.guardianBattleStates.stream().allMatch(x -> x.getCurrentHealth().get() < 1);
        final boolean wonGame = allGuardiansDead && finished.get() && !stageChangingToBoss;

        // handle matchplay reward
        if (!stageRewards.isEmpty() && !stageRewardsRelationships.isEmpty()) {
            for (Product product : stageRewards) {
                Double weight = stageRewardsRelationships.stream()
                        .filter(x -> x.getId_f().intValue() == product.getProductIndex())
                        .findFirst()
                        .map(SRelationships::getWeight)
                        .orElse(null);
                Integer qty = stageRewardsRelationships.stream()
                        .filter(x -> x.getId_f().intValue() == product.getProductIndex())
                        .findFirst()
                        .map(SRelationships::getQty)
                        .orElse(null);
                Integer qtyMin = stageRewardsRelationships.stream()
                        .filter(x -> x.getId_f().intValue() == product.getProductIndex())
                        .findFirst()
                        .map(SRelationships::getQtyMin)
                        .orElse(null);
                Integer qtyMax = stageRewardsRelationships.stream()
                        .filter(x -> x.getId_f().intValue() == product.getProductIndex())
                        .findFirst()
                        .map(SRelationships::getQtyMax)
                        .orElse(null);
                Integer levelReq = stageRewardsRelationships.stream()
                        .filter(x -> x.getId_f().intValue() == product.getProductIndex())
                        .findFirst()
                        .map(SRelationships::getLevelReq)
                        .orElse(null);
                Boolean forHardMode = stageRewardsRelationships.stream()
                        .filter(x -> x.getId_f().intValue() == product.getProductIndex())
                        .findFirst()
                        .map(SRelationships::getForHardMode)
                        .orElse(null);
                Boolean forRandomMode = stageRewardsRelationships.stream()
                        .filter(x -> x.getId_f().intValue() == product.getProductIndex())
                        .findFirst()
                        .map(SRelationships::getForRandomMode)
                        .orElse(null);

                if (forHardMode != null && forHardMode && !isHardMode.get()) {
                    continue;
                }

                if (forRandomMode != null && forRandomMode && !isRandomGuardiansMode.get()) {
                    continue;
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
                            qtyMax = wonGame ? 3 : 2;
                        else if (product.getCategory().equals(EItemCategory.QUICK.getName()))
                            qtyMax = wonGame ? 100 : 30;
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

                // allow only parts and lottery items for boss stages
                if (wonGame && isBoss) {
                    if (!product.getCategory().equals(EItemCategory.PARTS.getName()) && !product.getCategory().equals(EItemCategory.LOTTERY.getName())) {
                        continue;
                    }
                }

                if (!wonGame) {
                    // only material and quick items for lost games
                    if (!product.getCategory().equals(EItemCategory.MATERIAL.getName()) && !product.getCategory().equals(EItemCategory.QUICK.getName())) {
                        continue;
                    }

                    // qty is halved for lost games
                    qty = qty / 2;
                }

                itemRewards.add(new MatchplayReward.ItemReward(product.getProductIndex(), qty, weight));
            }
        }

        // also allow room for no item reward if it is not a boss stage
        if (!isBoss) {
            itemRewards.add(new MatchplayReward.ItemReward(0, 0, 15.0));
        }

        // avoid unnecessary empty item rewards
        if (itemRewards.isEmpty()) {
            itemRewards.add(new MatchplayReward.ItemReward(0, 0, 15.0));
        }

        double averageWeight = MatchplayReward.calculateAverageWeight(itemRewards);
        List<MatchplayReward.ItemReward> finalItemRewards = MatchplayReward.selectItemRewardsByWeight(itemRewards, 4, averageWeight);
        reward.addItemRewards(finalItemRewards);
        reward.assignItemRewardsToSlots(finalItemRewards);

        this.playerBattleStates.forEach(x -> {
            PlayerReward playerReward = new PlayerReward(x.getPosition());
            playerReward.setPlayerPosition(x.getPosition());

            double expMultiplier = expMultipliers.isEmpty() ? 1 : expMultipliers.stream().mapToDouble(SGuardianMultiplier::getMultiplier).sum();
            double goldMultiplier = goldMultipliers.isEmpty() ? 1 : goldMultipliers.stream().mapToDouble(SGuardianMultiplier::getMultiplier).sum();
            playerReward.setExp((int) (this.getExpPot().get() * expMultiplier));
            playerReward.setGold((int) (this.getGoldPot().get() * goldMultiplier));

            if (!wonGame) {
                playerReward.setExp(playerReward.getExp() / 2);
                playerReward.setGold(playerReward.getGold() / 2);
            }

            reward.addPlayerReward(playerReward);
        });

        return reward;
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

    public synchronized boolean loadAdvancedBossGuardianMode(String bossCode) {
        if (phaseManager != null) {
            log.error("Advanced boss guardian mode already loaded");
            return false;
        }

        Optional<ScriptManager> scriptManager = ScriptManagerFactory.loadScripts("scripts", () -> log);
        List<BossBattlePhaseable> phases = new ArrayList<>();
        if (scriptManager.isPresent()) {
            ScriptManager sm = scriptManager.get();
            List<ScriptFile> scriptFiles = sm.getScriptFiles("GUARDIAN-PHASE");
            for (ScriptFile scriptFile : scriptFiles) {
                String fileName = scriptFile.getFile().getName().split("_")[1].split("\\.")[0];

                if (!fileName.startsWith(bossCode))
                    continue;

                try {
                    Bindings bindings = sm.getScriptEngine().getBindings(ScriptContext.ENGINE_SCOPE);
                    bindings.put("gameManager", GameManager.getInstance());
                    bindings.put("serviceManager", GameManager.getInstance().getServiceManager());
                    bindings.put("threadManager", GameManager.getInstance().getThreadManager());
                    bindings.put("eventHandler", GameManager.getInstance().getEventHandler());
                    bindings.put("game", this);

                    BossBattlePhaseable phase = sm.getInterfaceByImplementingObject(scriptFile, "phase", BossBattlePhaseable.class, bindings);
                    phases.add(phase);
                } catch (Exception e) {
                    log.error("Error on register phase from script: " + fileName + ". ScriptException: " + e.getMessage(), e);
                }
            }
        }
        final boolean success = !phases.isEmpty();
        if (success) {
            this.phaseManager = new PhaseManager(phases);
            this.isAdvancedBossGuardianMode = true;
        }
        return success;
    }

    public GuardianBattleState getGuardianBattleStateByPosition(int position) {
        return this.guardianBattleStates.stream()
                .filter(x -> x.getPosition() == position)
                .findFirst()
                .orElse(null);
    }
}

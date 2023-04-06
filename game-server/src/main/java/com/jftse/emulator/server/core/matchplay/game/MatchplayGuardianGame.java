package com.jftse.emulator.server.core.matchplay.game;

import com.jftse.emulator.server.core.constants.BonusIconHighlightValues;
import com.jftse.emulator.server.core.constants.GameFieldSide;
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
import com.jftse.entities.database.model.battle.Guardian;
import com.jftse.entities.database.model.battle.GuardianBase;
import com.jftse.entities.database.model.battle.GuardianStage;
import com.jftse.entities.database.model.battle.WillDamage;
import com.jftse.entities.database.model.messenger.Friend;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.matchplay.battle.GuardianBattleState;
import com.jftse.server.core.matchplay.battle.PlayerBattleState;
import com.jftse.server.core.matchplay.battle.SkillCrystal;
import lombok.Getter;
import lombok.Setter;

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
import java.util.stream.Stream;

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
    private GuardianStage guardianStage;
    private GuardianStage bossGuardianStage;
    private GuardianStage currentStage;
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

    private final PlayerCombatSystem playerCombatSystem;
    private final GuardianCombatSystem guardianCombatSystem;

    public MatchplayGuardianGame() {
        super();

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        this.startTime = new AtomicReference<>(cal.getTime());
        this.stageStartTime = new AtomicReference<>(cal.getTime());
        this.playerLocationsOnMap = List.of(
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
    }

    public List<Byte> determineGuardians(GuardianStage guardianStage, int guardianLevelLimit) {
        List<Guardian> guardiansLeft = this.getFilteredGuardians(guardianStage.getGuardiansLeft(), guardianLevelLimit);
        List<Guardian> guardiansRight = this.getFilteredGuardians(guardianStage.getGuardiansRight(), guardianLevelLimit);
        List<Guardian> guardiansMiddle = this.getFilteredGuardians(guardianStage.getGuardiansMiddle(), guardianLevelLimit);

        byte leftGuardian = this.getRandomGuardian(guardiansLeft, new ArrayList<>());
        byte rightGuardian = this.getRandomGuardian(guardiansRight, List.of(leftGuardian));
        byte middleGuardian = this.getRandomGuardian(guardiansMiddle, Arrays.asList(leftGuardian, rightGuardian));
        if (middleGuardian != 0) {
            return Arrays.asList(middleGuardian, rightGuardian, leftGuardian);
        } else if (rightGuardian != 0) {
            return Arrays.asList(rightGuardian, leftGuardian, (byte) 0);
        }

        return Arrays.asList(leftGuardian, (byte) 0, (byte) 0);
    }

    public byte getRandomGuardian(List<Guardian> guardians, List<Byte> idsToIgnore) {
        byte guardianId = 0;
        if (guardians.size() > 0) {
            int amountsOfGuardiansToChooseFrom = guardians.size();
            if (amountsOfGuardiansToChooseFrom == 1) {
                return guardians.get(0).getId().byteValue();
            }

            Random r = new Random();
            int guardianIndex = r.nextInt(amountsOfGuardiansToChooseFrom);
            guardianId = guardians.get(guardianIndex).getId().byteValue();
            if (idsToIgnore.contains(guardianId)) {
                return getRandomGuardian(guardians, idsToIgnore);
            }
        }

        return guardianId;
    }

    public List<Guardian> getFilteredGuardians(List<Integer> ids, int guardianLevelLimit) {
        if (ids == null) {
            return new ArrayList<>();
        }

        List<Guardian> guardians = ServiceManager.getInstance().getGuardianService().findGuardiansByIds(ids);
        int lowestGuardianLevel = guardians.stream().min(Comparator.comparingInt(GuardianBase::getLevel)).get().getLevel();
        if (guardianLevelLimit < lowestGuardianLevel) {
            guardianLevelLimit = lowestGuardianLevel;
        }

        final int finalGuardianLevelLimit = guardianLevelLimit;
        return ServiceManager.getInstance().getGuardianService().findGuardiansByIds(ids).stream()
                .filter(x -> x.getLevel() <= finalGuardianLevelLimit)
                .collect(Collectors.toList());
    }

    public void fillRemainingGuardianSlots(boolean forceFill, MatchplayGuardianGame game, GuardianStage guardianStage, List<Byte> guardians) {
        int totalAvailableGuardianSlots = 3;
        int activeGuardianSlots = forceFill ? 0 : (int) guardians.stream().filter(x -> x != 0).count();
        int remainingGuardianSlots = totalAvailableGuardianSlots - activeGuardianSlots;
        if (game.getIsHardMode().get() && remainingGuardianSlots != 0) {
            List<Guardian> allGuardians = getAllGuardiansFromStage(guardianStage);
            for (int i = activeGuardianSlots; i < totalAvailableGuardianSlots; i++) {
                guardians.set(i, getRandomGuardian(allGuardians, guardians));
            }
        }
    }

    public List<Guardian> getAllGuardiansFromStage(GuardianStage guardianStage) {
        List<Integer> guardiansLeft = guardianStage.getGuardiansLeft();
        List<Integer> guardiansMiddle = guardianStage.getGuardiansMiddle();
        List<Integer> guardiansRight = guardianStage.getGuardiansRight();
        List<Integer> allGuardianIdsOfStage = Stream.of(
                        guardiansLeft != null ? guardiansLeft : new ArrayList<Integer>(),
                        guardiansMiddle != null ? guardiansMiddle : new ArrayList<Integer>(),
                        guardiansRight != null ? guardiansRight : new ArrayList<Integer>())
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());
        return ServiceManager.getInstance().getGuardianService().findGuardiansByIds(allGuardianIdsOfStage);
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
            return new GuardianBattleState(guardian.getId().intValue(), guardian.getBtItemID(), guardianPosition, 8000, 110, 45, 165, 120, guardian.getRewardExp(), guardian.getRewardGold());
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
        return new GuardianBattleState(guardian.getId().intValue(), guardian.getBtItemID(), guardianPosition, totalHp, totalStr, totalSta, totalDex, totalWill, guardian.getRewardExp(), guardian.getRewardGold());
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
        List<Integer> stageRewards = this.getCurrentStage().getRewards();
        List<PlayerReward> playerRewards = new ArrayList<>();

        final boolean stageChangingToBoss = this.stageChangingToBoss.get();
        final boolean allGuardiansDead = this.guardianBattleStates.stream().allMatch(x -> x.getCurrentHealth().get() < 1);
        final boolean wonGame = allGuardiansDead && !finished.get() && !stageChangingToBoss;

        this.playerBattleStates.forEach(x -> {
            PlayerReward playerReward = new PlayerReward(x.getPosition());
            playerReward.setPlayerPosition(x.getPosition());

            int expMultiplier = getGuardianStage().getExpMultiplier();
            playerReward.setExp(this.getExpPot().get() * expMultiplier);
            playerReward.setGold(this.getGoldPot().get());
            playerReward.setProductIndex(-1);

            if (stageRewards != null) {
                int rewardsCount = stageRewards.size();
                if (rewardsCount > 0) {
                    if (wonGame) {
                        Random r = new Random();
                        int itemRewardToGive = stageRewards.get(r.nextInt(rewardsCount));
                        playerReward.setProductIndex(itemRewardToGive);

                        int amount = this.getIsHardMode().get() ? 3 : 1;
                        if (this.getIsRandomGuardiansMode().get())
                            amount = 1;
                        playerReward.setProductAmount(amount);
                    }
                }
            } else {
                List<Integer> materialsForReward = ServiceManager.getInstance().getItemMaterialService().findAllItemIndexes();
                List<Integer> materialsProductIndex = ServiceManager.getInstance().getProductService().findAllProductIndexesByCategoryAndItemIndexList(EItemCategory.MATERIAL.getName(), materialsForReward);

                Random r = new Random();
                int drawnMaterial = r.nextInt(materialsProductIndex.size() - 1 + 1) + 1;

                playerReward.setProductIndex(materialsProductIndex.get(drawnMaterial - 1));

                final int min = 1;
                final int max = !wonGame ? 2 : 3;
                final int amount = r.nextInt(max - min + 1) + min;
                playerReward.setProductAmount(amount);
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

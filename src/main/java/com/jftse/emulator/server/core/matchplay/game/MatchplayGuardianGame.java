package com.jftse.emulator.server.core.matchplay.game;

import com.jftse.emulator.server.core.constants.GameFieldSide;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.matchplay.combat.GuardianCombatSystem;
import com.jftse.emulator.server.core.matchplay.combat.PlayerCombatSystem;
import com.jftse.emulator.server.core.matchplay.room.RoomPlayer;
import com.jftse.emulator.server.core.utils.BattleUtils;
import com.jftse.emulator.server.database.model.battle.Guardian;
import com.jftse.emulator.server.database.model.battle.GuardianBase;
import com.jftse.emulator.server.database.model.battle.GuardianStage;
import com.jftse.emulator.server.database.model.battle.WillDamage;
import com.jftse.emulator.server.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.core.matchplay.PlayerReward;
import com.jftse.emulator.server.core.matchplay.battle.GuardianBattleState;
import com.jftse.emulator.server.core.matchplay.battle.PlayerBattleState;
import com.jftse.emulator.server.core.matchplay.battle.SkillCrystal;

import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
public class MatchplayGuardianGame extends MatchplayGame {
    private final short guardianHealPercentage = 5; // Balancing purposes. Only ever heal 5% of guardians hp.
    public final static long guardianAttackLoopTime = TimeUnit.SECONDS.toMillis(8);

    private AtomicLong crystalSpawnInterval;
    private AtomicLong crystalDeSpawnInterval;
    private ConcurrentLinkedDeque<Point> playerLocationsOnMap;
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
    private int guardianLevelLimit;
    private Date stageStartTime;
    private AtomicInteger expPot;
    private AtomicInteger goldPot;
    private AtomicBoolean isHardMode;
    private AtomicBoolean randomGuardiansMode;
    private AtomicInteger spiderMineIdentifier;

    private final PlayerCombatSystem playerCombatSystem;
    private final GuardianCombatSystem guardianCombatSystem;

    public MatchplayGuardianGame() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        this.setStartTime(cal.getTime());
        this.setStageStartTime(cal.getTime());
        this.crystalSpawnInterval = new AtomicLong(0);
        this.crystalDeSpawnInterval = new AtomicLong(0);
        this.playerLocationsOnMap = new ConcurrentLinkedDeque<>(Arrays.asList(
                new Point(20, -75),
                new Point(-20, -75)
        ));
        this.playerBattleStates = new ConcurrentLinkedDeque<>();
        this.guardianBattleStates = new ConcurrentLinkedDeque<>();
        this.skillCrystals = new ConcurrentLinkedDeque<>();
        this.lastCrystalId = new AtomicInteger(-1);
        this.willDamages = new ArrayList<>();
        this.bossBattleActive = new AtomicBoolean(false);
        this.lastGuardianServeSide = new AtomicInteger(GameFieldSide.Guardian);
        this.expPot = new AtomicInteger(0);
        this.goldPot = new AtomicInteger(0);
        this.isHardMode = new AtomicBoolean(false);
        this.randomGuardiansMode = new AtomicBoolean(false);
        this.spiderMineIdentifier = new AtomicInteger(0);
        this.setFinished(new AtomicBoolean(false));

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
        long duration = cal.getTime().getTime() - this.getStageStartTime().getTime();
        return TimeUnit.MILLISECONDS.toSeconds(duration);
    }

    public void resetStageStartTime() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        setStageStartTime(cal.getTime());
    }

    public List<PlayerReward> getPlayerRewards() {
        List<Integer> stageRewards = this.getCurrentStage().getRewards();
        List<PlayerReward> playerRewards = new ArrayList<>();
        this.playerBattleStates.forEach(x -> {
            PlayerReward playerReward = new PlayerReward();
            playerReward.setPlayerPosition(x.getPosition().get());
            playerReward.setRewardExp(this.getExpPot().get());
            playerReward.setRewardGold(this.getGoldPot().get());
            playerReward.setRewardProductIndex(-1);

            if (stageRewards != null)
            {
                int rewardsCount = stageRewards.size();
                if (rewardsCount > 0) {
                    Random r = new Random();
                    int itemRewardToGive = stageRewards.get(r.nextInt(rewardsCount));
                    playerReward.setRewardProductIndex(itemRewardToGive);

                    int amount = this.isHardMode.get() ? 3 : 1;
                    playerReward.setProductRewardAmount(amount);
                }
            }

            playerRewards.add(playerReward);
        });

        return playerRewards;
    }

    @Override
    public long getTimeNeeded() {
        return getEndTime().getTime() - getStartTime().getTime();
    }

    @Override
    public boolean isRedTeam(int playerPos) {
        return false;
    }

    @Override
    public boolean isBlueTeam(int playerPos) {
        return false;
    }
}

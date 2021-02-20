package com.jftse.emulator.server.game.core.matchplay.basic;

import com.jftse.emulator.server.database.model.battle.GuardianStage;
import com.jftse.emulator.server.game.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.game.core.matchplay.battle.GuardianBattleState;
import com.jftse.emulator.server.game.core.matchplay.battle.PlayerBattleState;
import com.jftse.emulator.server.game.core.matchplay.battle.SkillCrystal;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class MatchplayGuardianGame extends MatchplayGame {
    private List<Point> playerLocationsOnMap;
    private List<PlayerBattleState> playerBattleStates;
    private List<GuardianBattleState> guardianBattleStates;
    private List<SkillCrystal> skillCrystals;
    private short lastCrystalId = -1;
    private GuardianStage guardianStage;
    private GuardianStage bossGuardianStage;
    private boolean bossBattleActive;
    private byte lastGuardianServeSide;
    private int guardianLevelLimit;
    private Date stageStartTime;

    public MatchplayGuardianGame() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        this.setStartTime(cal.getTime());
        this.setStageStartTime(cal.getTime());
        this.playerBattleStates = new ArrayList<>();
        this.guardianBattleStates = new ArrayList<>();
        this.skillCrystals = new ArrayList<>();
        this.playerLocationsOnMap = Arrays.asList(
                new Point(20, -75),
                new Point(-20, -75));
    }

    public short damageGuardian(short guardianPos, short damage) {
        GuardianBattleState guardianBattleState = this.guardianBattleStates.stream()
                .filter(x -> x.getPosition() == guardianPos)
                .findFirst()
                .orElse(null);
        short newGuardianHealth = (short) (guardianBattleState.getCurrentHealth() + damage);
        guardianBattleState.setCurrentHealth(newGuardianHealth);
        return newGuardianHealth;
    }

    public short damagePlayer(int playerPos, short damage) {
        PlayerBattleState playerBattleState = this.playerBattleStates.get(playerPos);
        short newPlayerHealth = (short) (playerBattleState.getCurrentHealth() + damage);
        playerBattleState.setCurrentHealth(newPlayerHealth);
        return newPlayerHealth;
    }

    public short healPlayer(int playerPos, short percentage) {
        PlayerBattleState playerBattleState = this.playerBattleStates.get(playerPos);
        short healthToHeal = (short) (playerBattleState.getMaxHealth() * (percentage / 100f));
        short newPlayerHealth = (short) (playerBattleState.getCurrentHealth() + healthToHeal);
        if (newPlayerHealth > playerBattleState.getMaxHealth()) {
            newPlayerHealth = playerBattleState.getMaxHealth();
        }

        playerBattleState.setCurrentHealth(newPlayerHealth);
        return newPlayerHealth;
    }

    public PlayerBattleState reviveAnyPlayer(short revivePercentage) {
        PlayerBattleState playerBattleState = getPlayerBattleStates().stream()
                .filter(x -> x.getCurrentHealth() < 1)
                .findFirst()
                .orElse(null);
        if (playerBattleState != null) {
            short newPlayerHealth = healPlayer(playerBattleState.getPosition(), revivePercentage);
            playerBattleState.setCurrentHealth(newPlayerHealth);
        }

        return playerBattleState;
    }

    public List<Short> assignSkillToPlayer(int playerPos, short skillIndex) {
        PlayerBattleState playerBattleState = this.playerBattleStates.get(playerPos);
        List<Short> playerSkills = playerBattleState.getSkillsStack();
        boolean skillSlotAvailable = playerSkills.stream().anyMatch(x -> x < 0);
        if (skillSlotAvailable) {
            int indexInArray = playerSkills.indexOf(playerSkills.stream().filter(x -> x < 0).findFirst().get());
            playerSkills.set(indexInArray, skillIndex);
            return playerSkills;
        }

        List<Short> playerSkillsCopy = new ArrayList<>(playerSkills);
        playerSkillsCopy.add(0, playerSkillsCopy.remove(playerSkillsCopy.size() - 1));
        playerSkillsCopy.set(1, skillIndex);
        playerBattleState.setSkillsStack(playerSkillsCopy);
        return playerSkillsCopy;
    }

    public List<Short> removeSkillFromTopOfStackFromPlayer(byte playerPos) {
        PlayerBattleState playerBattleState = this.playerBattleStates.get(playerPos);
        List<Short> playerSkills = playerBattleState.getSkillsStack();
        List<Short> playerSkillsCopy = new ArrayList<>(playerSkills);
        playerSkillsCopy.add(0, playerSkillsCopy.remove(playerSkillsCopy.size() - 1));
        playerSkillsCopy.set(1, (short) -1);
        playerBattleState.setSkillsStack(playerSkillsCopy);
        return playerSkillsCopy;
    }

    public long getStageTimePlayingInSeconds() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        long duration = cal.getTime().getTime() - this.getStageStartTime().getTime();
        return TimeUnit.MILLISECONDS.toSeconds(duration);
    }

    @Override
    public long getTimeNeeded() {
        return 0;
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

package com.ft.emulator.server.game.core.matchplay.basic;

import com.ft.emulator.server.game.core.matchplay.MatchplayGame;
import com.ft.emulator.server.game.core.matchplay.battle.PlayerBattleState;
import com.ft.emulator.server.game.core.matchplay.battle.SkillCrystal;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.util.*;
import java.util.List;

@Getter
@Setter
public class MatchplayGuardianGame extends MatchplayGame {
    private List<Point> playerLocationsOnMap;
    private List<PlayerBattleState> playerBattleStates;
    private List<SkillCrystal> skillCrystals;
    private short lastCrystalId = -1;

    private byte lastGuardianServeSide;

    public MatchplayGuardianGame() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        this.setStartTime(cal.getTime());
        this.skillCrystals = new ArrayList<>();
        this.playerLocationsOnMap = Arrays.asList(
                new Point(20, -75),
                new Point(-20, -75));
    }

    public short damagePlayer(int playerPos, short damage) {
        PlayerBattleState playerBattleState = this.playerBattleStates.get(playerPos);
        short newPlayerHealth = (short) (playerBattleState.getCurrentPlayerHealth() - damage);
        playerBattleState.setCurrentPlayerHealth(newPlayerHealth);
        return newPlayerHealth;
    }

    public List<Short> assignSkillToPlayer(int playerPos, short skillIndex) {
        PlayerBattleState playerBattleState = this.playerBattleStates.get(playerPos);
        List<Short> playerSkills = playerBattleState.getPlayerSkills();
        boolean skillSlotAvailable = playerSkills.stream().anyMatch(x -> x < 0);
        if (skillSlotAvailable) {
            int indexInArray = playerSkills.indexOf(playerSkills.stream().filter(x -> x < 0).findFirst().get());
            playerSkills.set(indexInArray, skillIndex);
            return playerSkills;
        }

        List<Short> playerSkillsCopy = new ArrayList<>(playerSkills);
        playerSkillsCopy.add(0, playerSkillsCopy.remove(playerSkillsCopy.size() - 1));
        playerSkillsCopy.set(1, skillIndex);
        playerBattleState.setPlayerSkills(playerSkillsCopy);
        return playerSkillsCopy;
    }

    public List<Short> removeSkillFromTopOfStackFromPlayer(byte playerPos) {
        PlayerBattleState playerBattleState = this.playerBattleStates.get(playerPos);
        List<Short> playerSkills = playerBattleState.getPlayerSkills();
        List<Short> playerSkillsCopy = new ArrayList<>(playerSkills);
        playerSkillsCopy.add(0, playerSkillsCopy.remove(playerSkillsCopy.size() - 1));
        playerSkillsCopy.set(1, (short) -1);
        playerBattleState.setPlayerSkills(playerSkillsCopy);
        return playerSkillsCopy;
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

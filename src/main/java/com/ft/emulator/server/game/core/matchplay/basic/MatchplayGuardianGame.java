package com.ft.emulator.server.game.core.matchplay.basic;

import com.ft.emulator.server.database.model.player.Player;
import com.ft.emulator.server.game.core.matchplay.MatchplayGame;
import com.ft.emulator.server.game.core.matchplay.PlayerHealth;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class MatchplayGuardianGame extends MatchplayGame {
    private List<Point> playerLocationsOnMap;
    private List<PlayerHealth> playerHPs;
    private byte lastGuardianServeSide;

    public MatchplayGuardianGame(List<PlayerHealth> playerHPs) {
        this.playerLocationsOnMap = Arrays.asList(
                new Point(20, -75),
                new Point(-20, -75));
        this.playerHPs = playerHPs;
    }

    public int damagePlayer(int playerPos, int damage) {
        PlayerHealth playerHealth = this.playerHPs.get(playerPos);
        playerHealth.setCurrentPlayerHealth(playerHealth.getCurrentPlayerHealth() - damage);
        if (playerHealth.getCurrentPlayerHealth() < 0) {
            playerHealth.setCurrentPlayerHealth(0);
        }

        this.playerHPs.set(playerPos, playerHealth);
        return playerHealth.getCurrentPlayerHealth() ;
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

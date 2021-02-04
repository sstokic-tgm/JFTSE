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

    public MatchplayGuardianGame() {
        this.playerLocationsOnMap = Arrays.asList(
                new Point(20, -75),
                new Point(-20, -75));
    }

    public short damagePlayer(int playerPos, short damage) {
        PlayerHealth playerHealth = this.playerHPs.get(playerPos);
        short newPlayerHealth = (short) (playerHealth.getCurrentPlayerHealth() - damage);
        playerHealth.setCurrentPlayerHealth(newPlayerHealth);
        return newPlayerHealth;
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

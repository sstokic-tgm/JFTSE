package com.ft.emulator.server.game.core.matchplay.basic;

import com.ft.emulator.server.game.core.matchplay.MatchplayGame;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class MatchplayGuardianGame extends MatchplayGame {
    private List<Point> playerLocationsOnMap;
    private byte lastGuardianServeSide;

    public MatchplayGuardianGame() {
        this.playerLocationsOnMap = Arrays.asList(
                new Point(20, -75),
                new Point(-20, -75));
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

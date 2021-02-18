package com.jftse.emulator.server.game.core.matchplay;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public abstract class MatchplayGame {
    private Date startTime;
    private Date endTime;

    private boolean finished;

    public abstract long getTimeNeeded();

    public abstract boolean isRedTeam(int playerPos);
    public abstract boolean isBlueTeam(int playerPos);
}
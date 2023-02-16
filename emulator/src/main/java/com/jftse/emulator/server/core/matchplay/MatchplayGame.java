package com.jftse.emulator.server.core.matchplay;

import com.jftse.entities.database.model.battle.WillDamage;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public abstract class MatchplayGame {
    private volatile Date startTime;
    private volatile Date endTime;

    protected List<WillDamage> willDamages;

    private boolean finished;

    public abstract long getTimeNeeded();

    public abstract boolean isRedTeam(int playerPos);
    public abstract boolean isBlueTeam(int playerPos);
}
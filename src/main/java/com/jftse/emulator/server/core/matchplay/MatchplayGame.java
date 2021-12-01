package com.jftse.emulator.server.core.matchplay;

import com.jftse.emulator.server.database.model.battle.WillDamage;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
@Setter
public abstract class MatchplayGame {
    private volatile Date startTime;
    private volatile Date endTime;

    protected List<WillDamage> willDamages;

    private AtomicBoolean finished;

    public abstract long getTimeNeeded();

    public abstract boolean isRedTeam(int playerPos);
    public abstract boolean isBlueTeam(int playerPos);
}
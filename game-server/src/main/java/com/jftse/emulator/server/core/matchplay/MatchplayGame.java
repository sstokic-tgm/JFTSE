package com.jftse.emulator.server.core.matchplay;

import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.entities.database.model.battle.WillDamage;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Getter
@Setter
public abstract class MatchplayGame {
    protected AtomicReference<Date> startTime;
    protected AtomicReference<Date> endTime;
    protected AtomicBoolean finished;
    protected ConcurrentLinkedDeque<ScheduledFuture<?>> scheduledFutures;

    protected List<WillDamage> willDamages;

    private MatchplayHandleable handleable;

    protected MatchplayGame() {
        this.handleable = this.createHandler();
    }

    public long getTimeNeeded() {
        return endTime.get().getTime() - startTime.get().getTime();
    }

    public boolean isRedTeam(int playerPos) {
        return playerPos == 0 || playerPos == 2;
    }

    public boolean isBlueTeam(int playerPos) {
        return playerPos == 1 || playerPos == 3;
    }

    public abstract MatchplayReward getMatchRewards();
    public abstract void addBonusesToRewards(ConcurrentLinkedDeque<RoomPlayer> roomPlayers, List<PlayerReward> playerRewards);

    protected abstract MatchplayHandleable createHandler();
}

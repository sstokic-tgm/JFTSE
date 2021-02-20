package com.jftse.emulator.server.game.core.singleplay.challenge;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public abstract class ChallengeGame {
    private int challengeIndex;

    private Date startTime;
    private Date endTime;

    private boolean finished;

    public abstract long getTimeNeeded();
}

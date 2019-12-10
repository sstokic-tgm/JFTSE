package com.ft.emulator.server.game.singleplay.challenge;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public abstract class ChallengeGame {

    private Long challengeId;

    private Date startTime;
    private Date endTime;

    private Boolean finished;

    public abstract long getTimeNeeded();
}
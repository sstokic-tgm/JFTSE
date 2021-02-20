package com.jftse.emulator.server.game.core.singleplay.tutorial;

import lombok.Getter;
import lombok.Setter;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

@Getter
@Setter
public class TutorialGame {
    private int tutorialIndex;

    private Date startTime;
    private Date endTime;

    private boolean finished;

    public TutorialGame(int tutorialIndex) {
        this.tutorialIndex = tutorialIndex;

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        this.startTime = cal.getTime();
        this.finished = false;
    }

    public void finishTutorial() {
        this.finished = true;

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        this.endTime = cal.getTime();
    }

    public long getTimeNeeded() {
        return getEndTime().getTime() - getStartTime().getTime();
    }
}

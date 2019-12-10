package com.ft.emulator.server.game.singleplay.tutorial;

import lombok.Getter;
import lombok.Setter;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

@Getter
@Setter
public class TutorialGame {

    private Long tutorialId;

    private Date startTime;
    private Date endTime;

    private Boolean finished;

    public TutorialGame(Long tutorialId) {

        this.tutorialId = tutorialId;

	Calendar currentCalender = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	this.startTime = currentCalender.getTime();

	this.finished = false;
    }

    public void finishTutorial() {

        this.finished = true;

	Calendar currentCalender = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	this.endTime = currentCalender.getTime();
    }
}
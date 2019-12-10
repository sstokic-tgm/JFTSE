package com.ft.emulator.server.game.singleplay.challenge;

import lombok.Getter;
import lombok.Setter;

import java.util.Calendar;
import java.util.TimeZone;

@Getter
@Setter
public class ChallengeBasicGame extends ChallengeGame {

    private Byte pointsPlayer;
    private Byte pointsNpc;
    private Byte setsPlayer;
    private Byte setsNpc;

    public ChallengeBasicGame(Long challengeId) {

        this.setChallengeId(challengeId);

	Calendar currentCalender = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        this.setStartTime(currentCalender.getTime());

        this.pointsPlayer = 0;
        this.pointsNpc = 0;
        this.setsPlayer = 0;
        this.setsNpc = 0;
        this.setFinished(false);
    }

    public void setPoints(byte pointsPlayer, byte pointsNpc) {

        this.pointsPlayer = pointsPlayer;
        this.pointsNpc = pointsNpc;

        if(pointsPlayer == 4 && pointsNpc < 4) {

	    this.setsPlayer++;
            this.pointsPlayer = 0;
            this.pointsNpc = 0;
	}
        else if(pointsPlayer > 4 && (pointsPlayer - pointsNpc) == 2) {

            this.setsPlayer++;
            this.pointsPlayer = 0;
            this.pointsNpc = 0;
	}
        else if(pointsNpc == 4 && pointsPlayer < 4) {

            this.setsNpc++;
            this.pointsPlayer = 0;
            this.pointsNpc = 0;
	}
        else if(pointsNpc > 4 && (pointsNpc - pointsPlayer) == 2) {

            this.setsNpc++;
            this.pointsPlayer = 0;
            this.pointsNpc = 0;
	}

        if(this.setsPlayer == 2 || this.setsNpc == 2) {

            this.setFinished(true);

	    Calendar currentCalender = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	    this.setEndTime(currentCalender.getTime());
	}
    }

    @Override
    public long getTimeNeeded() {
        return getEndTime().getTime() - getStartTime().getTime();
    }
}